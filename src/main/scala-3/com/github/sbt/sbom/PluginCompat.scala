// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

package com.github.sbt.sbom

import sbt.*

private[sbt] object PluginCompat {
  val integrationTest: Configuration =
    Configuration.of(
      "IntegrationTest",
      "it",
      "Integration tests",
      false,
      Vector(Runtime),
      true
    )
}
