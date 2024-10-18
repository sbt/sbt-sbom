package com.github.sbt.sbom.licenses

import scala.util.Try
import scala.xml.XML

class LicensesArchiveParser(textArchive: String) {
  private lazy val xmlTry = Try {
    XML.loadString(textArchive)
  }

  private lazy val licensesTry: Try[Seq[License]] =
    xmlTry.map { root =>
      (root \ "licenses").map { license =>
        val licenseId = (license \ "licenseId").text
        val refs = (license \ "seeAlso").map(_.text)
        License(id = Some(licenseId), references = refs)
      }
    }

  lazy val licenses: Seq[License] = licensesTry.getOrElse(Seq())

  lazy val isValid: Boolean = licensesTry.isSuccess
}
