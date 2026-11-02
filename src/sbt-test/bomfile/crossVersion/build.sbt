// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

lazy val root = (project in file("."))
  .settings(
    name := "crossVersion",
    version := "0.1",
    scalaVersion := "2.12.21",
    crossScalaVersions := Seq("2.12.21", "2.13.16")
  )
