import sbt._

object Dependencies {
  lazy val library = Seq(
    "org.cyclonedx"  % "cyclonedx-core-java" % "7.1.0",
    "org.scalatest" %% "scalatest"           % "3.2.9" % Test,
    "org.scalamock" %% "scalamock"           % "5.1.0" % Test
  )
}
