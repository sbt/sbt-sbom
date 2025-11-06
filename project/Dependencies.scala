// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

import sbt.*

object Dependencies {
  lazy val library = Seq(
    "io.circe"      %% "circe-generic"       % "0.14.15",
    "io.circe"      %% "circe-parser"        % "0.14.15",
    "org.cyclonedx"  % "cyclonedx-core-java" % "11.0.0",
    "org.scalatest" %% "scalatest"           % "3.2.19" % Test,
    "org.scalamock" %% "scalamock"           % "7.5.1"  % Test
  )
}
