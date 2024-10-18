package io.github.siculo.sbtbom

import io.github.siculo.sbtbom.PluginConstants._
import org.cyclonedx.model.Component
import sbt.Keys.{ artifact, configuration, version }
import sbt.{ Def, _ }

import scala.language.postfixOps

/**
 * plugin object
 */
object BomSbtPlugin extends AutoPlugin {

  override def requires: Plugins = empty

  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    lazy val bomFileName: SettingKey[String] = settingKey[String]("bom file name")
    lazy val bomSchemaVersion: SettingKey[String] = settingKey[String](
      s"bom schema version; must be one of ${supportedVersionsDescr}; default is ${defaultSupportedVersionDescr}"
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
    val bomFileNameSetting = Def.setting {
      val artifactId = artifact.value.name
      val artifactVersion = version.value
      s"${artifactId}-${artifactVersion}.bom.xml"
    }
    Seq(
      bomFileName := bomFileNameSetting.value,
      bomSchemaVersion := defaultSupportedVersion.getVersionString,
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
      bomConfigurations := Def.taskDyn(BomSbtSettings.bomConfigurationTask((configuration ?).value)).value
    )
  }
}
