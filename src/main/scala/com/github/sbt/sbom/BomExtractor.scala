// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

package com.github.sbt.sbom

import com.github.packageurl.PackageURL
import com.github.sbt.sbom.licenses.LicensesArchive
import org.cyclonedx.Version
import org.cyclonedx.model.{ Bom, Component, ExternalReference, Hash, License, LicenseChoice, Metadata, Tool }
import org.cyclonedx.util.BomUtils
import sbt._
import sbt.librarymanagement.ModuleReport

import java.util
import java.util.UUID
import scala.collection.JavaConverters._

class BomExtractor(settings: BomExtractorParams, report: UpdateReport, log: Logger) {
  private val serialNumber: String = "urn:uuid:" + UUID.randomUUID.toString

  def bom: Bom = {
    val bom = new Bom
    if (settings.includeBomSerialNumber && settings.schemaVersion != Version.VERSION_10) {
      bom.setSerialNumber(serialNumber)
    }
    if (settings.schemaVersion.getVersion >= Version.VERSION_12.getVersion) {
      bom.setMetadata(metadata)
    }
    bom.setComponents(components.asJava)
    bom
  }

  private lazy val metadata: Metadata = {
    val metadata = new Metadata()
    if (!settings.includeBomTimestamp) {
      metadata.setTimestamp(null)
    }
    metadata.addTool(tool)
    metadata
  }

  private lazy val tool: Tool = {
    val tool = new Tool()
    // https://github.com/devops-kung-fu/bomber/blob/main/lib/loader.go#L112 searches for string CycloneDX to detect format
    tool.setName("CycloneDX SBT plugin")
    if (settings.includeBomToolVersion) {
      tool.setVersion(BuildInfo.version)
    }
    tool
  }

  private def components: Seq[Component] = {
    val components = configurationsForComponents(settings.configuration).flatMap { configuration =>
      componentsForConfiguration(configuration)
    }.distinct // deduplicate components reported by multiple configurations
    components.groupBy(_.getBomRef).foreach {
      case (null, _)   => () // ignore empty bom-refs
      case (_, Seq(_)) => () // no duplicate bom-refs
      case (bomRef, components) => // duplicate bom-refs
        log.warn(s"bom-ref must be distinct: $bomRef")
        components.foreach(_.setBomRef(null))
    }
    components
  }

  private def configurationsForComponents(configuration: Configuration): Seq[sbt.Configuration] = {
    log.info(s"Current configuration = ${configuration.name}")
    configuration match {
      case Test =>
        Seq(Test, Runtime, Compile)
      case IntegrationTest =>
        Seq(IntegrationTest, Runtime, Compile)
      case Runtime =>
        Seq(Runtime, Compile)
      case Compile =>
        Seq(Compile)
      case Provided =>
        Seq(Provided)
      case anyOtherConfiguration: Configuration =>
        Seq(anyOtherConfiguration)
      case _ =>
        Seq()
    }
  }

  private def componentsForConfiguration(configuration: Configuration): Seq[Component] = {
    report
      .configuration(configuration)
      .map { configurationReport =>
        log.info(
          s"Configuration name = ${configurationReport.configuration.name}, modules: ${configurationReport.modules.size}"
        )
        configurationReport.modules.map { module =>
          new ComponentExtractor(module).component
        }
      }
      .getOrElse(Seq())
  }

  class ComponentExtractor(moduleReport: ModuleReport) {
    def component: Component = {
      val group: String = moduleReport.module.organization
      val name: String = moduleReport.module.name
      val version: String = moduleReport.module.revision
      /*
        moduleReport.extraAttributes found keys are:
          - "info.apiURL"
          - "info.versionScheme"
       */
      val component = new Component()
      component.setGroup(group)
      component.setName(name)
      component.setVersion(version)
      component.setModified(false)
      component.setType(Component.Type.LIBRARY)
      component.setPurl(
        new PackageURL(PackageURL.StandardTypes.MAVEN, group, name, version, new util.TreeMap(), null).canonicalize()
      )
      if (settings.schemaVersion.getVersion >= Version.VERSION_11.getVersion) {
        // component bom-refs must be unique
        component.setBomRef(component.getPurl)
      }
      component.setScope(Component.Scope.REQUIRED)
      if (settings.includeBomHashes) {
        component.setHashes(hashes(artifactPaths(moduleReport)).asJava)
      }
      licenseChoice.foreach(component.setLicenses)
      if (settings.includeBomExternalReferences && settings.schemaVersion.getVersion >= Version.VERSION_11.getVersion) {
        moduleReport.homepage.foreach { url =>
          val homepage = new ExternalReference()
          homepage.setType(ExternalReference.Type.WEBSITE)
          homepage.setUrl(url)
          component.addExternalReference(homepage)
        }
      }

      /*
        not returned component properties are (BOM version 1.0):
          - publisher: The person(s) or organization(s) that published the component
          - copyright: An optional copyright notice informing users of the underlying claims to copyright ownership in a published work.
          - cpe: Specifies a well-formed CPE name. See https://nvd.nist.gov/products/cpe
          - components: Specifies optional sub-components. This is not a dependency tree. It simply provides an optional way to group large sets of components together.
          - user defined attributes: User-defined attributes may be used on this element as long as they do not have the same name as an existing attribute used by the schema.
       */

      // logComponent(component)

      component
    }

    private def artifactPaths(moduleReport: ModuleReport): Seq[File] =
      moduleReport.artifacts
        .map { case (_, file) =>
          file
        }
        .filter { file =>
          file.exists() && file.isFile
        }

    private def hashes(files: Seq[File]): Seq[Hash] =
      files.flatMap { file =>
        val hashes = BomUtils.calculateHashes(file, settings.schemaVersion).asScala
        if (settings.enableBomSha3Hashes) {
          hashes
        } else {
          hashes.filterNot(_.getAlgorithm.matches("(?i)SHA3-.*"))
        }
      }

    private def licenseChoice: Option[LicenseChoice] = {
      val licensesArchive = LicensesArchive.bundled
      val licenses: Seq[License] = moduleReport.licenses.map { case (name, mayBeUrl) =>
        val license = new License()
        licensesArchive
          .findById(name)
          .orElse(mayBeUrl.map(licensesArchive.findByUrl).collect { case Seq(license) =>
            license
          })
          .foreach { archiveLicense =>
            license.setId(archiveLicense.id)
          }
        if (license.getId == null) {
          // must not be set if id is defined
          license.setName(name)
        }
        mayBeUrl.foreach { url =>
          if (settings.schemaVersion.getVersion >= Version.VERSION_11.getVersion) {
            license.setUrl(url)
          }
        }
        license
      }
      if (licenses.isEmpty) {
        None
      } else {
        val choice = new LicenseChoice()
        licenses.foreach(choice.addLicense)
        Some(choice)
      }
    }
  }

  def logComponent(component: Component): Unit = {
    log.info(s""""
         |${component.getGroup}" % "${component.getName}" % "${component.getVersion}",
         | Modified = ${component.getModified}, Component type = ${component.getType.getTypeName},
         | Scope = ${component.getScope.getScopeName}
         | """.stripMargin)
  }

}
