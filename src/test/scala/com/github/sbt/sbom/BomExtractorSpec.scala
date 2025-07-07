package com.github.sbt.sbom

import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen

import java.util.TreeMap as TM

class BomExtractorSpec extends AnyFeatureSpecLike with MockFactory with GivenWhenThen {
  Feature("Qualifier in PURL") {
    val mockExtractor = mock[BomExtractor]
    val packageName = "name"
    val packageGroup = "group"
    val packageVersion = "1.0.0"

    Scenario("No qualifier") {
      When("Given only Group, Name, Version.")
        val expectedUrl = "pkg:maven/group/name@1.0.0"
        val testedPurl = mockExtractor.enrichedPurl(packageGroup, packageName, packageVersion)
      Then("Purl must contain no qualifier")
        assert(testedPurl == expectedUrl)
    }

    Scenario("Type only") {
      When("When qualifier only contains type.")
        val qualifierTree = new TM[String, String]()
        qualifierTree.put("type", "jar")

        val expectedUrl = "pkg:maven/group/name@1.0.0?type=jar"
        val testedPurl = mockExtractor.enrichedPurl(packageGroup, packageName, packageVersion, qualifierTree)

      Then("Purl's qualifier must contain type")
        assert(testedPurl == expectedUrl)
    }

    Scenario("Classifier only") {
      When("When qualifier only contains classifier.")
        val qualifierTree = new TM[String, String]()
        qualifierTree.put("classifier", "native")

        val expectedUrl = "pkg:maven/group/name@1.0.0?classifier=native"
        val testedPurl = mockExtractor.enrichedPurl(packageGroup, packageName, packageVersion, qualifierTree)

      Then("Purl's qualifier must contain classifier")
        assert(testedPurl == expectedUrl)
    }

    Scenario("Type and Classifier") {
      When("When qualifier contains both classifier and type.")
        val qualifierTree = new TM[String, String]()
        qualifierTree.put("type", "jar")
        qualifierTree.put("classifier", "native")

        val expectedUrl = "pkg:maven/group/name@1.0.0?classifier=native&type=jar"
        val testedPurl = mockExtractor.enrichedPurl(packageGroup, packageName, packageVersion, qualifierTree)

      Then("Purl's qualifier must contain classifier and type")
        assert(testedPurl == expectedUrl)
    }
  }
}