import sbt._

object Dependencies {
  lazy val library = Seq(
    "io.circe"      %% "circe-generic"       % "0.14.10",
    "io.circe"      %% "circe-parser"        % "0.14.10",
    "org.cyclonedx"  % "cyclonedx-core-java" % "9.1.0",
    "org.scalatest" %% "scalatest"           % "3.2.19" % Test,
    "org.scalamock" %% "scalamock"           % "6.0.0"  % Test
  )
}
