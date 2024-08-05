package io.github.siculo.sbtbom.licenses

import io.github.siculo.sbtbom.licenses.LicensesArchive.normalizeUrl

import scala.io.Source

class LicensesArchive(licenses: Seq[License]) {
  private val licensesByNormalizedUrl: Map[String, License] =
    licenses.iterator.flatMap { license =>
      license.references.map { reference =>
        (normalizeUrl(reference), license)
      }
    }.toMap

  def findByNormalizedUrl(url: String): Option[License] = licensesByNormalizedUrl.get(normalizeUrl(url))

  def findById(id: String): Option[License] = licenses.find(_.id.contains(id))
}

object LicensesArchive {
  private def normalizeUrl(url: String): String = url
    .toLowerCase
    .replaceFirst("^https://", "http://")
    .replaceFirst("\\.html$", "")
    .replaceFirst("\\.txt$", "")

  private def loadResourceAsString(resource: String): String = {
    val fileStream = getClass.getResourceAsStream(resource)
    Source.fromInputStream(fileStream).mkString
  }

  def fromJsonString(json: String): LicensesArchive =
    new LicensesArchive(LicensesArchiveJsonParser.parseString(json))

  lazy val bundled: LicensesArchive =
    fromJsonString(loadResourceAsString("/licenses.json"))
}
