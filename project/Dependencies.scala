import sbt._

object Dependencies {
  lazy val library = Seq(
    "org.cyclonedx"  % "cyclonedx-core-java" % "9.1.0",
    "org.scalatest" %% "scalatest"           % "3.2.19" % Test,
    "org.scalamock" %% "scalamock"           % "5.2.0"  % Test
  )
}
