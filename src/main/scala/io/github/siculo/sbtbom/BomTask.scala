package io.github.siculo.sbtbom

import org.apache.commons.io.FileUtils
import org.cyclonedx.Version
import org.cyclonedx.generators.BomGeneratorFactory
import org.cyclonedx.model.Bom
import org.cyclonedx.parsers.{JsonParser, XmlParser}
import sbt.*

import java.nio.charset.Charset
import scala.collection.JavaConverters.*

case class BomTaskProperties(
                              report: UpdateReport,
                              currentConfiguration: Configuration,
                              log: Logger,
                              schemaVersion: String,
                              jsonFormat: Boolean
                            )

abstract class BomTask[T](protected val properties: BomTaskProperties) {

  def execute: T

  protected def getBomText: String = {
    val bom: Bom = new BomExtractor(
      schemaVersion,
      currentConfiguration,
      report,
      log
    ).bom
    val bomText: String = if (properties.jsonFormat) {
      BomGeneratorFactory.createJson(schemaVersion, bom).toJsonString
    } else {
      BomGeneratorFactory.createXml(schemaVersion, bom).toXmlString
    }
    logBomInfo(schemaVersion, currentConfiguration, bom)
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
      val message = s"The BOM file ${bomFile.getAbsolutePath} does not conform to the CycloneDX BOM standard as defined by the XSD"
      log.error(s"$message:")
      exceptions.foreach {
        exception =>
          log.error(s"- ${exception.getMessage}")
      }
      throw new BomError(message)
    }
  }

  protected def logBomInfo(schemaVersion: Version, configuration: Configuration, bom: Bom): Unit = {
    log.info(s"Schema version: ${schemaVersion.getVersionString}")
    log.info(s"Serial number : ${bom.getSerialNumber}")
    log.info(s"Scope         : ${configuration.id}")
  }

  protected def report: UpdateReport = properties.report

  protected def currentConfiguration: Configuration = properties.currentConfiguration

  protected def log: Logger = properties.log

  protected lazy val schemaVersion: Version =
    SchemaVersions.supportedVersionByName(properties.schemaVersion) match {
      case Some(foundVersion) => foundVersion
      case None =>
        val message = s"Unsupported schema version ${properties.schemaVersion}"
        log.error(message)
        throw new BomError(message)
    }
}
