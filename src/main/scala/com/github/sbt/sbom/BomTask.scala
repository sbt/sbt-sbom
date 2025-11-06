// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

package com.github.sbt.sbom

import com.github.sbt.sbom.PluginConstants.*
import org.apache.commons.io.FileUtils
import org.cyclonedx.Version
import org.cyclonedx.generators.BomGeneratorFactory
import org.cyclonedx.model.Bom
import org.cyclonedx.parsers.{ JsonParser, XmlParser }
import sbt.*

import java.nio.charset.Charset
import scala.collection.JavaConverters.*

final case class BomTaskProperties(
    report: UpdateReport,
    currentConfiguration: Configuration,
    rootModuleID: ModuleID,
    log: Logger,
    schemaVersion: String,
    bomFormat: BomFormat,
    includeBomSerialNumber: Boolean,
    includeBomTimestamp: Boolean,
    includeBomToolVersion: Boolean,
    includeBomHashes: Boolean,
    enableBomSha3Hashes: Boolean,
    includeBomExternalReferences: Boolean,
    includeBomDependencyTree: Boolean,
    projectType: ProjectType,
    bomOutputPath: sbt.File
)

abstract class BomTask[T](protected val properties: BomTaskProperties) {

  def execute: T

  protected def getBomText: String = {
    val params: BomExtractorParams = extractorParams(currentConfiguration)
    val bom: Bom = new BomExtractor(params, report, rootModuleID, log).bom
    val bomText: String = bomFormat match {
      case BomFormat.Json => BomGeneratorFactory.createJson(schemaVersion, bom).toJsonString.replaceAll("\r?\n", "\n")
      case BomFormat.Xml  => BomGeneratorFactory.createXml(schemaVersion, bom).toXmlString.replaceAll("\r?\n", "\n")
    }
    logBomInfo(params, bom)
    bomText
  }

  protected def writeToFile(destFile: File, text: String): Unit = {
    FileUtils.write(destFile, text, Charset.forName("UTF-8"), false)
  }

  protected def validateBomFile(bomFile: File): Unit = {
    val parser = bomFormat match {
      case BomFormat.Json => new JsonParser()
      case BomFormat.Xml  => new XmlParser()
    }
    val exceptions = parser.validate(bomFile, schemaVersion).asScala
    if (exceptions.nonEmpty) {
      val message =
        s"The BOM file ${bomFile.getAbsolutePath} does not conform to the CycloneDX BOM standard as defined by the Schema"
      log.error(s"$message:")
      exceptions.foreach { exception =>
        log.error(s"- ${exception.getMessage}")
      }
      throw new BomError(message)
    }
  }

  @throws[BomError]
  protected def raiseException(message: String): Unit = {
    log.error(message)
    throw new BomError(message)
  }

  private def extractorParams(currentConfiguration: Configuration): BomExtractorParams =
    BomExtractorParams(
      schemaVersion,
      currentConfiguration,
      includeBomSerialNumber,
      includeBomTimestamp,
      includeBomToolVersion,
      includeBomHashes,
      enableBomSha3Hashes,
      includeBomExternalReferences,
      includeBomDependencyTree,
      projectType,
      bomOutputPath
    )

  protected def logBomInfo(params: BomExtractorParams, bom: Bom): Unit = {
    log.info(s"Schema version: ${schemaVersion.getVersionString}")
    // log.info(s"Serial number : ${bom.getSerialNumber}")
    log.info(s"Scope         : ${params.configuration.id}")
  }

  protected def report: UpdateReport = properties.report

  protected def currentConfiguration: Configuration = properties.currentConfiguration

  protected def rootModuleID: ModuleID = properties.rootModuleID

  protected def log: Logger = properties.log

  protected lazy val schemaVersion: Version =
    supportedVersions.find(_.getVersionString == properties.schemaVersion) match {
      case Some(foundVersion) => foundVersion
      case None               =>
        val message = s"Unsupported schema version ${properties.schemaVersion}"
        log.error(message)
        throw new BomError(message)
    }

  protected lazy val bomFormat: BomFormat = properties.bomFormat

  protected lazy val includeBomSerialNumber: Boolean = properties.includeBomSerialNumber

  protected lazy val includeBomTimestamp: Boolean = properties.includeBomTimestamp

  protected lazy val includeBomToolVersion: Boolean = properties.includeBomToolVersion

  protected lazy val includeBomHashes: Boolean = properties.includeBomHashes

  protected lazy val enableBomSha3Hashes: Boolean = properties.enableBomSha3Hashes

  protected lazy val includeBomExternalReferences: Boolean = properties.includeBomExternalReferences

  protected lazy val includeBomDependencyTree: Boolean = properties.includeBomDependencyTree

  protected lazy val projectType: ProjectType = properties.projectType

  protected lazy val bomOutputPath: sbt.File = properties.bomOutputPath
}
