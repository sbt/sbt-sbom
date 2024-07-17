package io.github.siculo.sbtbom

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class SchemaVersionsSpec extends AnyWordSpec with Matchers {
  "PluginConstants" should {
    "return the description of the supported versions" in {
      SchemaVersions.supportedVersionsDescr shouldBe """"1.0", "1.1", "1.2", "1.3", "1.4", "1.5" or "1.6""""
    }
  }
}
