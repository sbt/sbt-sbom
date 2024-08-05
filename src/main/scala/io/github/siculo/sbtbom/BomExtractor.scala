package io.github.siculo.sbtbom

import com.github.packageurl.PackageURL
import org.cyclonedx.Version
import org.cyclonedx.model.{Hash, License, *}
import sbt.*
import _root_.io.github.siculo.sbtbom.licenses.LicensesArchive
import org.cyclonedx.util.BomUtils
import sbt.librarymanagement.ModuleReport

import java.util
import java.util.UUID
import scala.collection.JavaConverters.*

class BomExtractor(settings: BomExtractorParams, report: UpdateReport, log: Logger) {
  private val serialNumber: String = "urn:uuid:" + UUID.randomUUID.toString

  def bom: Bom = {
    val bom = new Bom
    if (settings.schemaVersion != Version.VERSION_10) {
      bom.setSerialNumber(serialNumber)
    }
    bom.setMetadata(metadata)
    bom.setComponents(components.asJava)
    bom
  }

  private def metadata: Metadata = {
    val metadata = new Metadata
    if (settings.schemaVersion.getVersion >= Version.VERSION_12.getVersion) {
      metadata.addTool(tool)
    }
    metadata
  }

  private def tool: Tool = {
    val tool = new Tool
    tool.setName("CycloneDX SBT plugin")
    tool.setVersion(BuildInfo.version)
    tool
  }

  private def components: Seq[Component] =
    configurationsForComponents(settings.configuration).foldLeft(Seq[Component]()) {
      case (collected, configuration) =>
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
    (report.configuration(configuration) map {
      configurationReport =>
        log.info(s"Configuration name = ${configurationReport.configuration.name}, modules: ${configurationReport.modules.size}")
        configurationReport.modules.map {
          module =>
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
      component.setHashes(hashes(artifactPaths(moduleReport)).asJava)
      licenseChoice.foreach(component.setLicenses)

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
      moduleReport.artifacts.map { case (_, file) =>
        file
      }.filter { file =>
        file.exists() && file.isFile
      }

    private def hashes(files: Seq[File]): Seq[Hash] =
      files.flatMap { file =>
        BomUtils.calculateHashes(file, settings.schemaVersion).asScala
      }

    private def licenseChoice: Option[LicenseChoice] = {
      val licenses: Seq[License] = moduleReport.licenses.map {
        case (name, urlOption) =>
          val license = new License()
          license.setName(name)
          urlOption.foreach { licenseUrl =>
            LicensesArchive.bundled.findByNormalizedUrl(licenseUrl).foreach { archiveLicense =>
              license.setId(archiveLicense.id)
              license.setName(archiveLicense.name)
            }
            if (settings.schemaVersion != Version.VERSION_10) {
              license.setUrl(licenseUrl)
            }
          }
          license
      }
      if (licenses.isEmpty) None
      else {
        val choice = new LicenseChoice()
        licenses.foreach(choice.addLicense)
        Some(choice)
      }
    }
  }

  private def logComponent(component: Component): Unit = {
    log.info(
      s""""
         |${component.getGroup}" % "${component.getName}" % "${component.getVersion}",
         | Modified = ${component.getModified}, Component type = ${component.getType.getTypeName},
         | Scope = ${component.getScope.getScopeName}
         | """.stripMargin)
  }

}
