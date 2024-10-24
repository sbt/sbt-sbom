package com.github.sbt.sbom

import com.github.packageurl.PackageURL
import org.cyclonedx.Version
import org.cyclonedx.model.{ Bom, Component, License, LicenseChoice }
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
    bom.setComponents(components.asJava)
    bom
  }

  private def components: Seq[Component] =
    configurationsForComponents(settings.configuration).foldLeft(Seq[Component]()) { case (collected, configuration) =>
      collected ++ componentsForConfiguration(configuration)
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
    (report.configuration(configuration) map { configurationReport =>
      log.info(
        s"Configuration name = ${configurationReport.configuration.name}, modules: ${configurationReport.modules.size}"
      )
      configurationReport.modules.map { module =>
        new ComponentExtractor(module).component
      }
    }).getOrElse(Seq())
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
      component.setScope(Component.Scope.REQUIRED)
      licenseChoice.foreach(component.setLicenseChoice)

      /*
        not returned component properties are (BOM version 1.0):
          - publisher: The person(s) or organization(s) that published the component
          - hashes
          - copyright: An optional copyright notice informing users of the underlying claims to copyright ownership in a published work.
          - cpe: Specifies a well-formed CPE name. See https://nvd.nist.gov/products/cpe
          - components: Specifies optional sub-components. This is not a dependency tree. It simply provides an optional way to group large sets of components together.
          - user defined attributes: User-defined attributes may be used on this element as long as they do not have the same name as an existing attribute used by the schema.
       */

      // logComponent(component)

      component
    }

    private def licenseChoice: Option[LicenseChoice] = {
      val licenses: Seq[model.License] = moduleReport.licenses.map { case (name, mayBeUrl) =>
        model.License(name, mayBeUrl)
      }
      if (licenses.isEmpty)
        None
      else {
        val choice = new LicenseChoice()
        licenses.foreach { modelLicense =>
          val license = new License()
          license.setName(modelLicense.name)
          if (settings.schemaVersion != Version.VERSION_10) {
            modelLicense.url.foreach(license.setUrl)
          }
          choice.addLicense(license)
        }
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
