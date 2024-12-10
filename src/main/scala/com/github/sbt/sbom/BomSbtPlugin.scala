package com.github.sbt.sbom

import com.github.sbt.sbom.PluginConstants._
import org.cyclonedx.Version
import org.cyclonedx.model.Component
import sbt.Keys.{ artifact, configuration, packagedArtifacts, version }
import sbt.plugins.JvmPlugin
import sbt.{ Def, _ }

import scala.language.postfixOps

/**
 * plugin object
 */
object BomSbtPlugin extends AutoPlugin {

  override def requires: Plugins = JvmPlugin

  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    lazy val bomFileName: SettingKey[String] = settingKey[String]("bom file name")
    lazy val bomSchemaVersion: SettingKey[String] = settingKey[String](
      s"bom schema version; must be one of ${supportedVersionsDescr}; default is ${defaultSupportedVersionDescr}"
    )
    lazy val bomFormat: SettingKey[String] = settingKey[String](
      "bom format; must be json or xml"
    )
    lazy val includeBomSerialNumber: SettingKey[Boolean] = settingKey[Boolean](
      "should the resulting BOM contain a serial number? default is false, because the current mechanism for determining the serial number is not reproducible"
    )
    lazy val makeBom: TaskKey[sbt.File] = taskKey[sbt.File]("Generates bom file")
    lazy val listBom: TaskKey[String] = taskKey[String]("Returns the bom")
    lazy val components: TaskKey[Component] = taskKey[Component]("Returns the bom")

    lazy val bomConfigurations: TaskKey[Seq[Configuration]] = taskKey[Seq[Configuration]](
      "Returns the list of configurations whose components are included in the generated bom"
    )
  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = {
    def defaultFormat(schemaVersion: String): String =
      supportedVersions.find(_.getVersionString == schemaVersion) match {
        case Some(foundVersion) if foundVersion.getVersion > Version.VERSION_11.getVersion => "json"
        case _                                                                             => "xml"
      }
    val bomFileNameSetting = Def.setting {
      val artifactId = artifact.value.name
      val artifactVersion = version.value
      val schemaVersion = bomSchemaVersion.value
      val format = bomFormat.??(defaultFormat(schemaVersion)).value
      s"${artifactId}-${artifactVersion}.bom.${format}"
    }
    val bomFormatFromFileNameSetting = Def.setting {
      val fileName = bomFileName.value
      val schemaVersion = bomSchemaVersion.value
      fileName.toLowerCase match {
        case ext if ext.endsWith(".xml")  => "xml"
        case ext if ext.endsWith(".json") => "json"
        case _                            => defaultFormat(schemaVersion)
      }
    }
    Seq(
      bomFileName := bomFileNameSetting.value,
      bomFileName / bomFormat := bomFormat.or(bomFormatFromFileNameSetting).value,
      bomSchemaVersion := defaultSupportedVersion.getVersionString,
      includeBomSerialNumber := false,
      makeBom := Def.taskDyn(BomSbtSettings.makeBomTask(Classpaths.updateTask.value, Compile)).value,
      listBom := Def.taskDyn(BomSbtSettings.listBomTask(Classpaths.updateTask.value, Compile)).value,
      Test / makeBom := Def.taskDyn(BomSbtSettings.makeBomTask(Classpaths.updateTask.value, Test)).value,
      Test / listBom := Def.taskDyn(BomSbtSettings.listBomTask(Classpaths.updateTask.value, Test)).value,
      IntegrationTest / makeBom := Def
        .taskDyn(BomSbtSettings.makeBomTask(Classpaths.updateTask.value, IntegrationTest))
        .value,
      IntegrationTest / listBom := Def
        .taskDyn(BomSbtSettings.listBomTask(Classpaths.updateTask.value, IntegrationTest))
        .value,
      bomConfigurations := Def.taskDyn(BomSbtSettings.bomConfigurationTask((configuration ?).value)).value,
      packagedArtifacts += {
        Artifact(artifact.value.name, "cyclonedx", "xml", "cyclonedx") -> makeBom.value
      },
    )
  }
}
