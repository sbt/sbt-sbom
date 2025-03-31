// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

import sbt._

object Dependencies {
  lazy val library = Seq(
    "io.circe"      %% "circe-generic"       % "0.14.12",
    "io.circe"      %% "circe-parser"        % "0.14.12",
    "org.cyclonedx"  % "cyclonedx-core-java" % "10.2.1",
    "org.scalatest" %% "scalatest"           % "3.2.19" % Test,
    "org.scalamock" %% "scalamock"           % "7.3.0"  % Test
  )
}
