// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

lazy val root = (project in file("."))
  .settings(
    name := "outputPath",
    version := "0.1",
    crossPaths := false,
    autoScalaLibrary := false,
    bomOutputPath := (baseDirectory.value / "does" / "not" / "exist").getAbsolutePath
  )
