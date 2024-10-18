package io.github.siculo.sbtbom.licenses

import scala.io.Source

class LicensesArchive(licenses: Seq[License]) {
  private val licensesByUrl: Map[String, License] = licenses.foldLeft(Map[String, License]()) { (map, license) =>
    map ++ license.references.foldLeft(Map[String, License]()) { (map, ref) =>
      map + (ref -> license)
    }
  }

  def findByUrl(url: String): Option[License] = licensesByUrl.get(url)

  def findById(id: String): Option[License] = licenses.find(_.id.contains(id))
}

object LicensesArchive {
  private lazy val fileStream = getClass.getResourceAsStream("/licenses.xml")
  private lazy val archiveText = Source.fromInputStream(fileStream).mkString
  private lazy val archive = new LicensesArchive(new LicensesArchiveParser(archiveText).licenses)

  def findByUrl(url: String): Option[License] = archive.findByUrl(url)
}
