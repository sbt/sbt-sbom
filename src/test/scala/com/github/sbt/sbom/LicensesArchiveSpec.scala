package com.github.sbt.sbom

import com.github.sbt.sbom.licenses.LicensesArchive
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class LicensesArchiveSpec extends AnyWordSpec with Matchers {
  "LicensesArchiveParser" should {
    "fail parsing a not valid archive" in {
      assertThrows[RuntimeException] {
        LicensesArchive.fromJsonString("")
      }
    }

    "parse a valid archive" in {
      LicensesArchive.fromJsonString(json)
    }
  }

  "LicenseRegister" should {
    "find no license by ref" in {
      val register = LicensesArchive.fromJsonString(json)
      register.findByNormalizedUrl("http://www.domain.com/missingLicense") shouldBe None
    }

    "find licenses by ref" in {
      val register = LicensesArchive.fromJsonString(json)
      val gps2 = register.findByNormalizedUrl("https://opensource.org/licenses/GPL-2.0")
      val zeroBsd = register.findByNormalizedUrl("http://landley.net/toybox/license.html")

      gps2.isDefined shouldBe true
      gps2.get.id shouldBe "GPL-2.0-or-later"
      zeroBsd.isDefined shouldBe true
      zeroBsd.get.id shouldBe "0BSD"
    }

    "find no licenses by id" in {
      val register = LicensesArchive.fromJsonString(json)
      register.findById("an invalid id") shouldBe None
    }

    "shoud read licenses from resource file" in {
      val gpl2OrLater = LicensesArchive.bundled.findByNormalizedUrl("https://opensource.org/licenses/GPL-2.0")
      gpl2OrLater.isDefined shouldBe true
      gpl2OrLater.get.id shouldBe "GPL-2.0-or-later"
    }

    "find licenses by id" in {
      val register = LicensesArchive.fromJsonString(json)
      val gpl2 = register.findById("GPL-2.0-or-later")
      gpl2.isDefined shouldBe true
      gpl2.get.id shouldBe "GPL-2.0-or-later"
    }
  }

  lazy val json: String =
    """{
      |  "licenseListVersion": "b5a3b2e",
      |  "licenses": [
      |    {
      |      "reference": "https://spdx.org/licenses/0BSD.html",
      |      "isDeprecatedLicenseId": false,
      |      "detailsUrl": "https://spdx.org/licenses/0BSD.json",
      |      "referenceNumber": 430,
      |      "name": "BSD Zero Clause License",
      |      "licenseId": "0BSD",
      |      "seeAlso": [
      |        "http://landley.net/toybox/license.html",
      |        "https://opensource.org/licenses/0BSD"
      |      ],
      |      "isOsiApproved": true
      |    },
      |    {
      |      "reference": "https://spdx.org/licenses/GPL-2.0-or-later.html",
      |      "isDeprecatedLicenseId": false,
      |      "detailsUrl": "https://spdx.org/licenses/GPL-2.0-or-later.json",
      |      "referenceNumber": 629,
      |      "name": "GNU General Public License v2.0 or later",
      |      "licenseId": "GPL-2.0-or-later",
      |      "seeAlso": [
      |        "https://www.gnu.org/licenses/old-licenses/gpl-2.0-standalone.html",
      |        "https://opensource.org/licenses/GPL-2.0"
      |      ],
      |      "isOsiApproved": true,
      |      "isFsfLibre": true
      |    }
      |  ],
      |  "releaseDate": "2024-06-28"
      |}
    """.stripMargin
}
