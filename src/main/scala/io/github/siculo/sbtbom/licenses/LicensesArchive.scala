package io.github.siculo.sbtbom.licenses

import scala.io.Source

class LicensesArchive(licenses: Seq[License]) {
  private val licensesByUrl: Map[String, License] = licenses.foldLeft(Map[String, License]()) {
    (map, license) =>
      map ++ license.references.foldLeft(Map[String, License]()) {
        (map, ref) =>
          map + (ref -> license)
      }
  }

  def findByUrl(url: String): Option[License] = licensesByUrl.get(url)

  def findById(id: String): Option[License] = licenses.find(_.id.contains(id))
}

object LicensesArchive {
  private def loadResourceAsString(resource: String): String = {
    val fileStream = getClass.getResourceAsStream(resource)
    Source.fromInputStream(fileStream).mkString
  }

  def fromJsonString(json: String): LicensesArchive =
    new LicensesArchive(LicensesArchiveJsonParser.parseString(json))

  lazy val bundled: LicensesArchive =
    fromJsonString(loadResourceAsString("/licenses.json"))
}
