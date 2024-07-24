ThisBuild / organization := "de.lhns"
ThisBuild / version := {
  val Tag = "refs/tags/v?([0-9]+(?:\\.[0-9]+)+(?:[+-].*)?)".r
  sys.env
    .get("CI_VERSION")
    .collect { case Tag(tag) => tag }
    .getOrElse("0.4.0-SNAPSHOT")
}
ThisBuild / scalaVersion := "2.12.19"

ThisBuild / licenses += ("MIT License", url("https://opensource.org/licenses/MIT"))
ThisBuild / homepage := scmInfo.value.map(_.browseUrl)
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/lhns/sbt-bom"),
    "scm:git@github.com:lhns/sbt-bom.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id = "siculo",
    name = "Fabrizio Di Giuseppe",
    email = "siculo.github@gmail.com",
    url = url("https://github.com/siculo")
  ),
  Developer(
    id = "lhns",
    name = "Pierre Kisters",
    email = "pierrekisters@gmail.com",
    url = url("https://github.com/lhns/")
  )
)
ThisBuild / description := "SBT plugin to generate CycloneDx SBOM files"

lazy val root = (project in file("."))
  .enablePlugins(ScriptedPlugin)
  .settings(
    name := "sbt-bom",
    sbtPlugin := true,
    libraryDependencies ++= Seq(
      "org.cyclonedx" % "cyclonedx-core-java" % "9.0.4",
      "io.circe" %% "circe-generic" % "0.14.9",
      "io.circe" %% "circe-parser" % "0.14.9",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "org.scalamock" %% "scalamock" % "6.0.0" % Test
    ),
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++ Seq("-Xmx1024M", "-Dplugin.version=" + version.value, "-Dplugin.organization=" + organization.value)
    },
    scriptedBufferLog := false
  )

ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true
