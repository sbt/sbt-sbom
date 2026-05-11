// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

lazy val root = (project in file("."))
  .enablePlugins(BomSbtPlugin)
  .settings(
    name := "exists",
    version := "0.1",
    libraryDependencies ++= Dependencies.library,
    scalaVersion := "2.12.21"
  )
