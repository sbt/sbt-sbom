// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

package com.github.sbt.sbom

import org.cyclonedx.Version
import sbt.Configuration

final case class BomExtractorParams(
    schemaVersion: Version,
    configuration: Configuration,
    includeBomSerialNumber: Boolean,
    includeBomTimestamp: Boolean,
    includeBomToolVersion: Boolean,
    includeBomHashes: Boolean,
    enableBomSha3Hashes: Boolean,
    includeBomExternalReferences: Boolean,
    includeBomDependencyTree: Boolean,
    projectType: String,
    bomOutputPath: String
)
