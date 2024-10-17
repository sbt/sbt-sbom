package io.github.siculo.sbtbom

import org.cyclonedx.Version
import sbt.Configuration

case class BomExtractorParams(schemaVersion: Version, configuration: Configuration)
