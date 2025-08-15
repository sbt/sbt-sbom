// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

package com.github.sbt.sbom

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BomExtractorSpec extends AnyWordSpec with Matchers {
  val packageName = "name"
  val packageGroup = "group"
  val packageVersion = "1.0.0"

  "PURL" should {
    "contain no qualifier" in {
      val testedPurl = BomExtractor.purl(packageGroup, packageName, packageVersion)
      assertResult("pkg:maven/group/name@1.0.0") {
        testedPurl
      }
    }

    "contain Type only" in {
      val qualifierTree = Map[String, String]("type" -> "pom")
      val testedPurl = BomExtractor.purl(packageGroup, packageName, packageVersion, qualifierTree)

      assertResult("pkg:maven/group/name@1.0.0?type=pom") {
        testedPurl
      }
    }

    "contain Classifier only" in {
      val qualifierTree = Map[String, String]("classifier" -> "native")
      val testedPurl = BomExtractor.purl(packageGroup, packageName, packageVersion, qualifierTree)

      assertResult("pkg:maven/group/name@1.0.0?classifier=native") {
        testedPurl
      }
    }

    "contain both Type and Classifier" in {
      val qualifierTree = Map[String, String]("type" -> "pom", "classifier" -> "native")
      val testedPurl = BomExtractor.purl(packageGroup, packageName, packageVersion, qualifierTree)

      assertResult("pkg:maven/group/name@1.0.0?classifier=native&type=pom") {
        testedPurl
      }
    }
  }
}

