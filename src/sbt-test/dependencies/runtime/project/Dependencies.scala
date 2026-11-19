// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

import sbt.*

object Dependencies {

  private val circeVersion = "0.10.0"
  private val scalatestVersion = "3.0.5"

  lazy val library = Seq(
    "io.circe"      %% "circe-core"    % circeVersion,
    "io.circe"      %% "circe-generic" % circeVersion,
    "io.circe"      %% "circe-parser"  % circeVersion,
    "org.scalatest" %% "scalatest"     % scalatestVersion % Test
  )

}
