package io.github.siculo.sbtbom.licenses

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.parser.*

import scala.util.control.NonFatal

object LicensesArchiveJsonParser {
  private case class LicenseJson(
                                  licenseId: String,
                                  name: String,
                                  seeAlso: Seq[String]
                                )

  private object LicenseJson {
    implicit val decoder: Decoder[LicenseJson] = deriveDecoder
  }

  private case class LicensesArchiveJson(
                                          licenses: Seq[LicenseJson]
                                        )

  private object LicensesArchiveJson {
    implicit val decoder: Decoder[LicensesArchiveJson] = deriveDecoder
  }

  private def licenseFromLicenseEntry(licenseEntry: LicenseJson): License = License(
    id = licenseEntry.licenseId,
    name = licenseEntry.name,
    references = licenseEntry.seeAlso
  )

  def parseString(string: String): Seq[License] = {
    val licensesArchiveJson = try {
      decode[LicensesArchiveJson](string).toTry.get
    } catch {
      case NonFatal(e) => throw new RuntimeException("failed to parse licenses archive json", e)
    }

    licensesArchiveJson.licenses.map(licenseFromLicenseEntry)
  }
}
