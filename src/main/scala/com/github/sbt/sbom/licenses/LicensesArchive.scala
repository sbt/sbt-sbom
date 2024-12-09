package com.github.sbt.sbom.licenses

import com.github.sbt.sbom.licenses.LicensesArchive.{ normalizeId, normalizeUrl }

import scala.io.Source

class LicensesArchive(licenses: Seq[License]) {
  private val licensesByNormalizedUrl: Map[String, Seq[License]] =
    licenses.iterator
      .flatMap { license =>
        license.references.map { reference =>
          (normalizeUrl(reference), license)
        }
      }
      .toList
      .groupBy(_._1)
      .mapValues(_.map(_._2))

  private val licenseByNormalizedId: Map[String, License] =
    licenses
      .groupBy(license => normalizeId(license.id))
      .mapValues {
        case Seq(license) => license
        case licenses     => throw new RuntimeException(s"conflicting licenses: $licenses")
      }

  def findByUrl(url: String): Seq[License] = licensesByNormalizedUrl.getOrElse(normalizeUrl(url), Seq.empty)

  def findById(id: String): Option[License] = licenseByNormalizedId.get(normalizeId(id))
}

object LicensesArchive {
  private def normalizeUrl(url: String): String = url.toLowerCase
    .replaceFirst("^https?://(www\\.)?", "https://")
    .replaceFirst("/$", "")
    .replaceFirst("\\.(html|txt)$", "")

  // Apache-2.0 will be normalized to apache 2, BSD 3-Clause will be normalized to bsd 3 clause
  private def normalizeId(id: String): String = id.toLowerCase
    .replace("-", " ")
    .replaceFirst("(?<=\\d)\\.0", "")

  private def loadResourceAsString(resource: String): String = {
    val fileStream = getClass.getResourceAsStream(resource)
    Source.fromInputStream(fileStream).mkString
  }

  def fromJsonString(json: String): LicensesArchive =
    new LicensesArchive(LicensesArchiveJsonParser.parseString(json))

  lazy val bundled: LicensesArchive =
    fromJsonString(loadResourceAsString("/licenses.json"))
}
