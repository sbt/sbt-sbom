package io.github.siculo.sbtbom

import org.cyclonedx.Version

object SchemaVersions {
  val supportedVersions: Seq[Version] = Seq(
    Version.VERSION_10,
    Version.VERSION_11,
    Version.VERSION_12,
    Version.VERSION_13,
    Version.VERSION_14,
    Version.VERSION_15,
    Version.VERSION_16
  )

  val defaultSupportedVersion: Version = Version.VERSION_12

  def supportedVersionByName(name: String): Option[Version] =
    supportedVersions.find(_.getVersionString == name)

  private def schemaVersionDescr(version: Version): String =
    s""""${version.getVersionString}""""

  val supportedVersionsDescr: String =
    supportedVersions.dropRight(1).map(schemaVersionDescr).mkString(", ") + " or " + schemaVersionDescr(supportedVersions.last)

  val defaultSupportedVersionDescr: String = schemaVersionDescr(defaultSupportedVersion)
}
