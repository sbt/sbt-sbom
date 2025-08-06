// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

package com.github.sbt.sbom

import com.github.packageurl.PackageURL
import com.github.sbt.sbom.BomExtractor.purl
import com.github.sbt.sbom.licenses.LicensesArchive
import org.cyclonedx.Version
import org.cyclonedx.model.{
  Bom,
  Component,
  Dependency,
  ExternalReference,
  Hash,
  License,
  LicenseChoice,
  Metadata,
  Tool
}
import org.cyclonedx.util.BomUtils
import sbt._
import sbt.librarymanagement.ModuleReport

import java.util.{UUID, TreeMap as TM}
import scala.collection.JavaConverters._

import SbtUpdateReport.{ModuleGraph, getModuleQualifier}

class BomExtractor(settings: BomExtractorParams, report: UpdateReport, rootModuleID: ModuleID, log: Logger) {
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
    if (settings.includeBomDependencyTree && settings.schemaVersion.getVersion >= Version.VERSION_11.getVersion) {
      bom.setDependencies(dependencyTree.asJava)
    }
    bom
  }

  private lazy val metadata: Metadata = {
    val metadata = new Metadata()
    if (!settings.includeBomTimestamp) {
      metadata.setTimestamp(null)
    }
    metadata.addTool(tool)
    metadata.setComponent(metadataComponent)
    metadata
  }

  private lazy val metadataComponent: Component = {
    val metadataComponent = new Component ()
    val group : String = rootModuleID.organization
    val name : String = rootModuleID.name
    val version : String = rootModuleID.revision

    metadataComponent.setGroup(group)
    metadataComponent.setName(name)
    metadataComponent.setBomRef(purl(group, name, version))
    metadataComponent.setVersion(version)
    metadataComponent.setType(toCycloneDxProjectType(settings.projectType))
    metadataComponent.setPurl(purl(group, name, version))

    metadataComponent
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
      case (null, _)            => () // ignore empty bom-refs
      case (_, Seq(_))          => () // no duplicate bom-refs
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

      component.setPurl(purl(group, name, version, getModuleQualifier(moduleReport, Some(log))))
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

  private def dependencyTree: Seq[Dependency] = {
    val dependencyTree = configurationsForComponents(settings.configuration).flatMap { configuration =>
      dependencyTreeForConfiguration(configuration)
    }.distinct // deduplicate dependencies reported by multiple configurations

    dependencyTree
  }

  private def dependencyTreeForConfiguration(configuration: Configuration): Seq[Dependency] = {
    report
      .configuration(configuration)
      .toSeq
      .flatMap { configurationReport =>
        new DependencyTreeExtractor(configurationReport).dependencyTree
      }
  }

  class DependencyTreeExtractor(configurationReport: ConfigurationReport) {
    def dependencyTree: Seq[Dependency] = {
      moduleGraph.nodes
        .filter(_.evictedByVersion.isEmpty)
        .sortBy(_.id.idString)
        .map { node =>
          val bomRef = purl(node.id.organization, node.id.name, node.id.version, node.qualifier)

          val dependency = new Dependency(bomRef)

          val dependsOn = moduleGraph.dependencyMap.getOrElse(node.id, Nil).sortBy(_.id.idString)
          dependsOn.foreach { module =>
            if (module.evictedByVersion.isEmpty){
              val bomRef = purl(module.id.organization, module.id.name, module.id.version, module.qualifier)

              dependency.addDependency(new Dependency(bomRef))
            }
          }

          dependency
        }
    }

    private def moduleGraph: ModuleGraph = SbtUpdateReport.fromConfigurationReport(configurationReport, rootModuleID, log)
  }

  private def toCycloneDxProjectType(e: ProjectType): Component.Type = {
    e match {
      case APPLICATION            => Component.Type.APPLICATION
      case FRAMEWORK              => Component.Type.FRAMEWORK
      case LIBRARY                => Component.Type.LIBRARY
      case CONTAINER              => Component.Type.CONTAINER
      case PLATFORM               => Component.Type.PLATFORM
      case OPERATING_SYSTEM       => Component.Type.OPERATING_SYSTEM
      case DEVICE                 => Component.Type.DEVICE
      case DEVICE_DRIVER          => Component.Type.DEVICE_DRIVER
      case FIRMWARE               => Component.Type.FIRMWARE
      case FILE                   => Component.Type.FILE
      case MACHINE_LEARNING_MODEL => Component.Type.MACHINE_LEARNING_MODEL
      case DATA                   => Component.Type.DATA
      case CRYPTOGRAPHIC_ASSET    =>
        if (settings.schemaVersion.getVersion < Version.VERSION_16.getVersion)
          throw new UnsupportedOperationException(
            "Current cyclonedx version does not support CRYPTOGRAPHIC_ASSET. Use 1.6 or newer"
          )
        else Component.Type.CRYPTOGRAPHIC_ASSET

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

object BomExtractor {
  private[sbom] def purl(group: String, name: String, version: String, qualifier: Map[String, String] = Map[String, String]()): String = {
    val convertedMap = new TM[String, String](qualifier.asJava)

    new PackageURL(PackageURL.StandardTypes.MAVEN, group, name, version, convertedMap, null).canonicalize()
  }
}

