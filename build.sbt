ThisBuild / organization := "io.github.siculo"
ThisBuild / organizationName := "Siculo"
ThisBuild / organizationHomepage := Some(url("https://github.com/siculo"))
ThisBuild / version := "0.4.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.19"
ThisBuild / homepage := Some(url("https://github.com/siculo/sbt-bom"))
ThisBuild / developers := List(
  Developer("siculo", "Fabrizio Di Giuseppe", "siculo.github@gmail.com", url("https://github.com/siculo"))
)
ThisBuild / licenses := List(
  ("MIT License", url("https://opensource.org/licenses/MIT"))
)
ThisBuild / scmInfo := Some(ScmInfo(
  url("https://github.com/siculo/sbt-bom/tree/master"),
  "scm:git:git://github.com/siculo/sbt-bom.git",
  Some("scm:git:ssh://github.com:siculo/sbt-bom.git")
))
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
    scriptedBufferLog := false,
    //dependencyOverrides += "org.typelevel" %% "jawn-parser" % "0.14.1"
  )

ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true
