package com.github.sbt.sbom

import org.cyclonedx.Version
import sbt.Configuration

case class BomExtractorParams(
    schemaVersion: Version,
    configuration: Configuration,
    includeBomSerialNumber: Boolean,
)
