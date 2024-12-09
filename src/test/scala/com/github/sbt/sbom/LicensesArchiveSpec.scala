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
      register.findByUrl("http://www.domain.com/missingLicense") shouldBe Seq.empty
    }

    "find licenses by ref" in {
      val register = LicensesArchive.fromJsonString(json)
      val gpl2 = register.findByUrl("https://opensource.org/licenses/GPL-2.0")
      val zeroBsd = register.findByUrl("http://landley.net/toybox/license.html")

      gpl2.size shouldBe 1
      gpl2.head.id shouldBe "GPL-2.0"
      zeroBsd.size shouldBe 1
      zeroBsd.head.id shouldBe "0BSD"
    }

    "find no licenses by id" in {
      val register = LicensesArchive.fromJsonString(json)
      register.findById("an invalid id") shouldBe None
    }

    "shoud read licenses from resource file" in {
      val gpl2 = LicensesArchive.bundled.findByUrl("https://opensource.org/licenses/GPL-2.0")
      gpl2.nonEmpty shouldBe true
      gpl2.exists(_.id == "GPL-2.0") shouldBe true
    }

    "find licenses by id" in {
      val register = LicensesArchive.fromJsonString(json)
      val gpl2 = register.findById("GPL-2.0")
      gpl2.isDefined shouldBe true
      gpl2.get.id shouldBe "GPL-2.0"
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
      |      "reference": "https://spdx.org/licenses/GPL-2.0.html",
      |      "isDeprecatedLicenseId": true,
      |      "detailsUrl": "https://spdx.org/licenses/GPL-2.0.json",
      |      "referenceNumber": 47,
      |      "name": "GNU General Public License v2.0 only",
      |      "licenseId": "GPL-2.0",
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
