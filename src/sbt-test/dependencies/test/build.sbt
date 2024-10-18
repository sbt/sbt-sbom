import scala.xml.XML

lazy val root = (project in file("."))
  .settings(
    name := "dependencies",
    version := "0.1",
    libraryDependencies ++= Dependencies.library,
    Test / bomFileName := "bom.xml",
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
  val bomFile = (Test / makeBom).value
  val context = thisProject.value
  val expected = XML.loadFile(file(s"${context.base}/etc/bom.xml"))
  s.log.info(s"${bomFile.getPath}")
  val actual = XML.loadFile(bomFile)
  val expectedComponents = expected \ "components"
  val actualComponents = actual \ "components"
  require(expectedComponents == actualComponents, s"${context.id} is failed.")
  s.log.info(s"${bomFile.getPath} content verified")
}
