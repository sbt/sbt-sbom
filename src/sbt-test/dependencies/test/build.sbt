// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

lazy val root = (project in file("."))
  .enablePlugins(BomSbtPlugin)
  .settings(
    name := "dependencies",
    version := "0.1",
    libraryDependencies ++= Dependencies.library,
    Test / bomFileName := "bom.xml",
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
  val bomFile = (Test / makeBom).value

  import scala.sys.process._
  val changed = Seq("diff", "-w", bomFile.getPath, s"${thisProject.value.base}/etc/bom.xml").! != 0
  if (changed) {
    if (sys.env.get("UPDATE").contains("true")) {
      // scripted tests are executed with PWD still pointing at the parent project:
      require(Seq("cp", bomFile.getPath, s"${sys.env("PWD")}/src/sbt-test/dependencies/test/etc/bom.xml").! == 0)
    }
    scala.sys.exit(1)
  }
}
