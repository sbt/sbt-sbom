package io.github.siculo.sbtbom

import org.cyclonedx.CycloneDxSchema

object PluginConstants {
  val supportedVersions: Seq[CycloneDxSchema.Version] = Seq(
    CycloneDxSchema.Version.VERSION_10,
    CycloneDxSchema.Version.VERSION_11,
    CycloneDxSchema.Version.VERSION_12,
    CycloneDxSchema.Version.VERSION_13,
    CycloneDxSchema.Version.VERSION_14
  )
  val defaultSupportedVersion = CycloneDxSchema.Version.VERSION_10
  val supportedVersionsDescr: String = {
    supportedVersions
      .take(supportedVersions.size - 1)
      .map(schemaVersionDescr)
      .mkString(", ") + " or " + schemaVersionDescr(supportedVersions.last)
  }
  val defaultSupportedVersionDescr: String = schemaVersionDescr(defaultSupportedVersion)

  private def schemaVersionDescr(version: CycloneDxSchema.Version): String = {
    s""""${version.getVersionString}""""
  }
}
