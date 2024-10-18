lazy val root = (project in file("."))
  .settings(
    name := "dependencies",
    version := "0.1",
    libraryDependencies ++= Dependencies.library,
    scalaVersion := "2.12.20",
    bomFileName := "bom.xml"
  )
