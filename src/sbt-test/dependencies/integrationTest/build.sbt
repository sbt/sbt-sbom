// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

lazy val root = (project in file("."))
  .settings(
    name := "dependencies",
    version := "0.1",
    libraryDependencies ++= Dependencies.library,
    IntegrationTest / bomFileName := "bom.xml",
    includeBomToolVersion := false,
    enableBomSha3Hashes := false,
    scalaVersion := "2.12.20",
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
  val bomFile = (IntegrationTest / makeBom).value

  import scala.sys.process._
  require(Seq("diff", "-w", bomFile.getPath, s"${thisProject.value.base}/etc/bom.xml").! == 0)
  s.log.info(s"${bomFile.getPath} content verified")
}
