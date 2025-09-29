// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

import sbt._

object Dependencies {
  lazy val library = Seq(
    "io.circe"      %% "circe-generic"       % "0.14.14",
    "io.circe"      %% "circe-parser"        % "0.14.14",
    "org.cyclonedx"  % "cyclonedx-core-java" % "11.0.0",
    "org.scalatest" %% "scalatest"           % "3.2.19" % Test,
    "org.scalamock" %% "scalamock"           % "7.5.0"  % Test
  )
}
