// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

import sbt.*

object Dependencies {
  lazy val library = Seq(
    "io.circe"      %% "circe-generic"       % "0.14.15",
    "io.circe"      %% "circe-parser"        % "0.14.15",
    "org.cyclonedx"  % "cyclonedx-core-java" % "12.2.0",
    "org.scalatest" %% "scalatest"           % "3.2.20" % Test,
    "org.scalamock" %% "scalamock"           % "7.5.5"  % Test
  )
}
