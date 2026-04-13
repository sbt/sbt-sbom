// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

lazy val root = (project in file("."))
  .settings(
    name := "application",
    organization := "org.example",
    version := "0.1",
    bomFileName := "bom.json",
    projectType := "application",
    includeBomToolVersion := false,
    enableBomSha3Hashes := false,
    includeBomHashes := false,
    includeBomExternalReferences := false,
    includeBomDependencyTree := false,
    scalaVersion := "2.12.21",
    check := Def
      .sequential(
        Compile / clean,
        Compile / compile,
        checkTask
      )
      .value
  )

lazy val check = taskKey[Unit]("check")
lazy val checkTask = Def.task {
  val s: TaskStreams = streams.value
  s.log.info("Verifying component type in bom content...")
  val bomFile = makeBom.value
  val content = scala.io.Source.fromFile(bomFile).mkString
  if (!content.contains(""""type" : "application"""")) {
    scala.sys.error(s"Expected component type 'application' not found in BOM:\n$content")
  }
}
