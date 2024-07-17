package io.github.siculo.sbtbom

import org.cyclonedx.{CycloneDxSchema, Version}
import sbt.Configuration

case class BomExtractorParams(schemaVersion: Version,
                              configuration: Configuration)
