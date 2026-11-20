// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

lazy val app = (projectMatrix in file("app"))
  .settings(
    name := "app",
    version := "0.1"
  )
  .jvmPlatform(scalaVersions = Seq("2.12.21", "2.13.18", "3.9.0"))
