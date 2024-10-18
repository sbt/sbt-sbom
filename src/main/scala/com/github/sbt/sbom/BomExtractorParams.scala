package com.github.sbt.sbom

import org.cyclonedx.Version
import sbt.Configuration

final case class BomExtractorParams(
    schemaVersion: Version,
    configuration: Configuration,
    includeBomSerialNumber: Boolean,
)
