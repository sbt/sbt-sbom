// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

package com.github.sbt.sbom

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PluginConstantsSpec extends AnyWordSpec with Matchers {
  "PluginConstants" should {
    "return the description of the supported versions" in {
      PluginConstants.supportedVersionsDescr shouldBe """"1.0", "1.1", "1.2", "1.3", "1.4", "1.5" or "1.6""""
    }
  }
}
