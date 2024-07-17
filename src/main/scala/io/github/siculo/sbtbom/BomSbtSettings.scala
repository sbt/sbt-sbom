package io.github.siculo.sbtbom

import io.github.siculo.sbtbom.BomSbtPlugin.autoImport.*
import sbt.*
import sbt.Keys.{sLog, target}

object BomSbtSettings {
  def makeBomTask(report: UpdateReport, currentConfiguration: Configuration): Def.Initialize[Task[sbt.File]] = Def.task[File] {
    new MakeBomTask(
      BomTaskProperties(report, currentConfiguration, sLog.value, bomSchemaVersion.value),
      target.value / (currentConfiguration / bomFileName).value
    ).execute
  }

  def listBomTask(report: UpdateReport, currentConfiguration: Configuration): Def.Initialize[Task[String]] =
    Def.task[String] {
      new ListBomTask(BomTaskProperties(report, currentConfiguration, sLog.value, bomSchemaVersion.value)).execute
    }

  def bomConfigurationTask(currentConfiguration: Option[Configuration]): Def.Initialize[Task[Seq[Configuration]]] =
    Def.task[Seq[Configuration]] {
      val log: Logger = sLog.value
      val usedConfiguration: Configuration = currentConfiguration match {
        case Some(c) =>
          log.info(s"Using configuration ${c.name}")
          c
        case None =>
          log.info(s"Using default configuration ${Compile.name}")
          Compile
      }
      usedConfiguration match {
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

}
