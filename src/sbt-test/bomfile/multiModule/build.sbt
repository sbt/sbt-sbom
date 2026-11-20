// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

lazy val root = (project in file("."))
  .aggregate(moduleA)
  .settings(
    name := "multiModule",
    version := "0.1",
    crossPaths := false,
    autoScalaLibrary := false
  )

lazy val moduleA = (project in file("module-a"))
  .settings(
    name := "module-a",
    version := "0.1",
    crossPaths := false,
    autoScalaLibrary := false
  )
