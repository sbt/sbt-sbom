package com.github.sbt.sbom

import org.cyclonedx.Version

object PluginConstants {
  val supportedVersions: Seq[Version] = Version.values()
  val defaultSupportedVersion: Version = supportedVersions.last
  val supportedVersionsDescr: String = {
    supportedVersions
      .take(supportedVersions.size - 1)
      .map(schemaVersionDescr)
      .mkString(", ") + " or " + schemaVersionDescr(supportedVersions.last)
  }
  val defaultSupportedVersionDescr: String = schemaVersionDescr(defaultSupportedVersion)

  private def schemaVersionDescr(version: Version): String = {
    s""""${version.getVersionString}""""
  }
}
