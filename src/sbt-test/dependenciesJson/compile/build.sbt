// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

lazy val root = (project in file("."))
  .settings(
    name := "dependencies",
    organization := "organization",
    version := "0.1",
    libraryDependencies ++= Dependencies.library,
    bomFileName := "bom.json",
    projectType := "application",
    includeBomToolVersion := false,
    enableBomSha3Hashes := false,
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
  s.log.info("Verifying bom content...")
  makeBom.value
  import scala.sys.process._
  require(Seq("diff", "-w", "target/bom.json", s"${thisProject.value.base}/etc/bom.json").! == 0)
}
