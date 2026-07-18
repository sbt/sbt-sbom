// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

ThisBuild / organization := "org.example"
ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.12.21"

lazy val root = (project in file("."))
  .dependsOn(compileSupport)
  .dependsOn(testSupport % "test->test")
  .settings(
    name := "root",
    bomFileName := "compile-bom.xml",
    Test / bomFileName := "test-bom.xml",
    includeBomToolVersion := false,
    includeBomHashes := false,
    includeBomExternalReferences := false,
    check := Def
      .sequential(
        clean,
        checkTask
      )
      .value
  )

lazy val compileSupport = (project in file("compile-support"))
  .dependsOn(compileTransitive)
  .settings(name := "compile-support")

lazy val compileTransitive = (project in file("compile-transitive"))
  .settings(name := "compile-transitive")

lazy val testSupport = (project in file("test-support"))
  .dependsOn(testTransitive)
  .settings(name := "test-support")

lazy val testTransitive = (project in file("test-transitive"))
  .settings(name := "test-transitive")

lazy val check = taskKey[Unit]("check")
lazy val checkTask = Def.task {
  val compileBom = IO.read(makeBom.value)
  assert(compileBom.contains("compile-support"), "Compile SBOM must include compile project dependencies")
  assert(compileBom.contains("compile-transitive"), "Compile SBOM must include transitive compile dependencies")
  assert(!compileBom.contains("test-support"), "Compile SBOM must exclude test-only project dependencies")
  assert(!compileBom.contains("test-transitive"), "Compile SBOM must exclude transitive test-only dependencies")

  val testBom = IO.read((Test / makeBom).value)
  assert(testBom.contains("test-support"), "Test SBOM must include test project dependencies")
  assert(testBom.contains("test-transitive"), "Test SBOM must include transitive test dependencies")
}
