// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

package com.github.sbt.sbom

import sbt.librarymanagement.Configurations

private[sbt] object PluginCompat {
  val integrationTest: sbt.Configuration = Configurations.IntegrationTest
}
