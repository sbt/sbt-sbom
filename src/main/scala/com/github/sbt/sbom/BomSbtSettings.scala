// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

package com.github.sbt.sbom

import com.github.sbt.sbom.BomSbtPlugin.autoImport._
import sbt.Keys.{ projectID, sLog, scalaBinaryVersion, scalaVersion, target }
import sbt._

object BomSbtSettings {
  def makeBomTask(report: UpdateReport, currentConfiguration: Configuration): Def.Initialize[Task[sbt.File]] =
    Def.task[File] {
      val format = BomFormat.fromSettings(
        (currentConfiguration / bomFormat).?.value,
        (currentConfiguration / bomFileName).?.value,
        bomSchemaVersion.value
      )

      val outputPath = if (bomOutputPath.value.isEmpty) {
        target.value
      } else {
        sbt.file(bomOutputPath.value)
      }

      val projType = ProjectType.fromString(projectType.value)
      new MakeBomTask(
        BomTaskProperties(
          report,
          currentConfiguration,
          CrossVersion(scalaVersion.value, scalaBinaryVersion.value)(
            projectID.value
          ),
          sLog.value,
          bomSchemaVersion.value,
          format,
          includeBomSerialNumber.value,
          includeBomTimestamp.value,
          includeBomToolVersion.value,
          includeBomHashes.value,
          enableBomSha3Hashes.value,
          includeBomExternalReferences.value,
          includeBomDependencyTree.value,
          projType,
          outputPath
        ),
        outputPath / (currentConfiguration / bomFileName).value
      ).execute
    }

  def listBomTask(report: UpdateReport, currentConfiguration: Configuration): Def.Initialize[Task[String]] =
    Def.task[String] {
      val format = BomFormat.fromSettings(
        (currentConfiguration / bomFormat).?.value,
        (currentConfiguration / bomFileName).?.value,
        bomSchemaVersion.value
      )
      val projType = ProjectType.fromString(projectType.value)
      new ListBomTask(
        BomTaskProperties(
          report,
          currentConfiguration,
          CrossVersion(scalaVersion.value, scalaBinaryVersion.value)(
            projectID.value
          ),
          sLog.value,
          bomSchemaVersion.value,
          format,
          includeBomSerialNumber.value,
          includeBomTimestamp.value,
          includeBomToolVersion.value,
          includeBomHashes.value,
          enableBomSha3Hashes.value,
          includeBomExternalReferences.value,
          includeBomDependencyTree.value,
          projType,
          sbt.file(bomOutputPath.value)
        )
      ).execute
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

