package com.github.sbt.sbom

import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.GivenWhenThen

class BomExtractorSpec extends AnyFeatureSpecLike with GivenWhenThen {
  Feature("Qualifier in PURL") {
    val packageName = "name"
    val packageGroup = "group"
    val packageVersion = "1.0.0"

    Scenario("No qualifier") {
      When("Given only Group, Name, Version.")
        val testedPurl = BomExtractor.enrichedPurl(packageGroup, packageName, packageVersion)
        
      Then("Purl must contain no qualifier")
        assertResult("pkg:maven/group/name@1.0.0") {
          testedPurl
        }
    }

    Scenario("Type only") {
      When("When qualifier only contains type.")
        val qualifierTree = Map[String, String]("type" -> "jar")
        val testedPurl = BomExtractor.enrichedPurl(packageGroup, packageName, packageVersion, qualifierTree)

      Then("Purl's qualifier must contain type")
        assertResult("pkg:maven/group/name@1.0.0?type=jar") {
          testedPurl
        }
    }

    Scenario("Classifier only") {
      When("When qualifier only contains classifier.")
        val qualifierTree = Map[String, String]("classifier" -> "native")
        val testedPurl = BomExtractor.enrichedPurl(packageGroup, packageName, packageVersion, qualifierTree)

      Then("Purl's qualifier must contain classifier")
        assertResult("pkg:maven/group/name@1.0.0?classifier=native"){
          testedPurl
        }
    }

    Scenario("Type and Classifier") {
      When("When qualifier contains both classifier and type.")
        val qualifierTree = Map[String, String]("type" -> "jar", "classifier" -> "native")
        val testedPurl = BomExtractor.enrichedPurl(packageGroup, packageName, packageVersion, qualifierTree)

      Then("Purl's qualifier must contain classifier and type")
        assertResult("pkg:maven/group/name@1.0.0?classifier=native&type=jar") {
          testedPurl
        }
    }
  }
}
