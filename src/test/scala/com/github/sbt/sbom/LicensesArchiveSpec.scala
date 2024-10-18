package com.github.sbt.sbom

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import com.github.sbt.sbom.licenses.{ LicensesArchive, LicensesArchiveParser }

class LicensesArchiveSpec extends AnyWordSpec with Matchers {
  "LicensesArchiveParser" should {
    "fail parsing a not valid archive" in {
      new LicensesArchiveParser("").isValid shouldBe false
    }

    "parse a valid archive" in {
      new LicensesArchiveParser(xml).isValid shouldBe true
    }
  }

  "LicenseRegister" should {
    "find no license by ref" in {
      val register = new LicensesArchive(new LicensesArchiveParser(xml).licenses)
      register.findByUrl("http://www.domain.com/missingLicense") shouldBe None
    }

    "find licenses by ref" in {
      val register = new LicensesArchive(new LicensesArchiveParser(xml).licenses)
      val gps2 = register.findByUrl("http://www.opensource.org/licenses/GPL-2.0")
      val zeroBsd = register.findByUrl("http://landley.net/toybox/license.html")

      gps2.isDefined shouldBe true
      gps2.get.id shouldBe Some("GPL-2.0")
      zeroBsd.isDefined shouldBe true
      zeroBsd.get.id shouldBe Some("0BSD")
    }

    "find no licenses by id" in {
      val register = new LicensesArchive(new LicensesArchiveParser(xml).licenses)
      register.findById("an invalid id") shouldBe None
    }

    "shoud read licenses from resource file" in {
      val gpl2OrLater = LicensesArchive.findByUrl("https://opensource.org/licenses/GPL-2.0")
      gpl2OrLater.isDefined shouldBe true
      gpl2OrLater.get.id shouldBe Some("GPL-2.0")
    }

    "find licenses by id" in {
      val register = new LicensesArchive(new LicensesArchiveParser(xml).licenses)
      val gpl2 = register.findById("GPL-2.0")
      gpl2.isDefined shouldBe true
      gpl2.get.id shouldBe Some("GPL-2.0")
    }
  }

  val xml =
    """
      |<root>
      |  <licenseListVersion>v3.4-5-gb3d735f</licenseListVersion>
      |  <licenses>
      |    <reference>./0BSD.html</reference>
      |    <isDeprecatedLicenseId>false</isDeprecatedLicenseId>
      |    <detailsUrl>http://spdx.org/licenses/0BSD.json</detailsUrl>
      |    <referenceNumber>310</referenceNumber>
      |    <name>BSD Zero Clause License</name>
      |    <licenseId>0BSD</licenseId>
      |    <seeAlso>http://landley.net/toybox/license.html</seeAlso>
      |    <isOsiApproved>true</isOsiApproved>
      |  </licenses>
      |  <licenses>
      |    <reference>./GPL-2.0.html</reference>
      |    <isDeprecatedLicenseId>true</isDeprecatedLicenseId>
      |    <isFsfLibre>true</isFsfLibre>
      |    <detailsUrl>http://spdx.org/licenses/GPL-2.0.json</detailsUrl>
      |    <referenceNumber>140</referenceNumber>
      |    <name>GNU General Public License v2.0 only</name>
      |    <licenseId>GPL-2.0</licenseId>
      |    <seeAlso>http://www.gnu.org/licenses/old-licenses/gpl-2.0-standalone.html</seeAlso>
      |    <seeAlso>http://www.opensource.org/licenses/GPL-2.0</seeAlso>
      |    <isOsiApproved>true</isOsiApproved>
      |  </licenses>
      |  <releaseDate>2019-01-16</releaseDate>
      |</root>
    """.stripMargin
}
