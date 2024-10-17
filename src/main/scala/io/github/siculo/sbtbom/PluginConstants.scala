package io.github.siculo.sbtbom

import org.cyclonedx.Version

object PluginConstants {
  val supportedVersions: Seq[Version] = Seq(
    Version.VERSION_10,
    Version.VERSION_11,
    Version.VERSION_12,
    Version.VERSION_13,
    Version.VERSION_14
  )
  val defaultSupportedVersion = Version.VERSION_10
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
