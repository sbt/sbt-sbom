package com.github.sbt.sbom

import com.github.sbt.sbom.PluginConstants._
import org.apache.commons.io.FileUtils
import org.cyclonedx.Version
import org.cyclonedx.generators.BomGeneratorFactory
import org.cyclonedx.model.Bom
import org.cyclonedx.parsers.{ JsonParser, XmlParser }
import sbt._

import java.nio.charset.Charset
import scala.collection.JavaConverters._

final case class BomTaskProperties(
    report: UpdateReport,
    currentConfiguration: Configuration,
    log: Logger,
    schemaVersion: String,
    jsonFormat: Boolean,
    includeBomSerialNumber: Boolean,
)

abstract class BomTask[T](protected val properties: BomTaskProperties) {

  def execute: T

  protected def getBomText: String = {
    val params: BomExtractorParams = extractorParams(currentConfiguration)
    val bom: Bom = new BomExtractor(params, report, log).bom
    val bomText: String = if (properties.jsonFormat) {
      BomGeneratorFactory.createJson(schemaVersion, bom).toJsonString
    } else {
      BomGeneratorFactory.createXml(schemaVersion, bom).toXmlString
    }
    logBomInfo(params, bom)
    bomText
  }

  protected def writeToFile(destFile: File, text: String): Unit = {
    FileUtils.write(destFile, text, Charset.forName("UTF-8"), false)
  }

  protected def validateBomFile(bomFile: File): Unit = {
    val parser = if (properties.jsonFormat) {
      new JsonParser()
    } else {
      new XmlParser()
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
    BomExtractorParams(schemaVersion, currentConfiguration, includeBomSerialNumber)

  protected def logBomInfo(params: BomExtractorParams, bom: Bom): Unit = {
    log.info(s"Schema version: ${schemaVersion.getVersionString}")
    // log.info(s"Serial number : ${bom.getSerialNumber}")
    log.info(s"Scope         : ${params.configuration.id}")
  }

  protected def report: UpdateReport = properties.report

  protected def currentConfiguration: Configuration = properties.currentConfiguration

  protected def log: Logger = properties.log

  protected lazy val schemaVersion: Version =
    supportedVersions.find(_.getVersionString == properties.schemaVersion) match {
      case Some(foundVersion) => foundVersion
      case None =>
        val message = s"Unsupported schema version ${properties.schemaVersion}"
        log.error(message)
        throw new BomError(message)
    }

  protected lazy val includeBomSerialNumber: Boolean = properties.includeBomSerialNumber
}
