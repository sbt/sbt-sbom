import sbt.Keys._

lazy val root = (project in file("."))
  .settings(
    name := "dependencies",
    version := "0.1",
    libraryDependencies ++= Dependencies.library,
    Test / bomFileName := "bom.xml",
    scalaVersion := "2.12.20",
    bomSchemaVersion := "999",
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
  s.log.info("Verifying makeBom param validation...")
  (Test / makeBom).value
}
