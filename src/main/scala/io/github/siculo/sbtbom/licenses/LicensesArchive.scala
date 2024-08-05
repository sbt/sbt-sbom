package io.github.siculo.sbtbom.licenses

import io.github.siculo.sbtbom.licenses.LicensesArchive.normalizeProtocol

import scala.io.Source

class LicensesArchive(licenses: Seq[License]) {
  private val licensesByUrlIgnoreProtocol: Map[String, License] =
    licenses.iterator.flatMap { license =>
      license.references.map { reference =>
        (normalizeProtocol(reference), license)
      }
    }.toMap

  def findByUrlIgnoreProtocol(url: String): Option[License] = licensesByUrlIgnoreProtocol.get(normalizeProtocol(url))

  def findById(id: String): Option[License] = licenses.find(_.id.contains(id))
}

object LicensesArchive {
  private def normalizeProtocol(url: String): String = url.replaceFirst("^https://", "http://")

  private def loadResourceAsString(resource: String): String = {
    val fileStream = getClass.getResourceAsStream(resource)
    Source.fromInputStream(fileStream).mkString
  }

  def fromJsonString(json: String): LicensesArchive =
    new LicensesArchive(LicensesArchiveJsonParser.parseString(json))

  lazy val bundled: LicensesArchive =
    fromJsonString(loadResourceAsString("/licenses.json"))
}
