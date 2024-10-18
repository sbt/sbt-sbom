ThisBuild / organization := Organization.organization
ThisBuild / organizationName := Organization.organizationName
ThisBuild / organizationHomepage := Organization.organizationHomepage
ThisBuild / version := "0.4.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.19"
ThisBuild / homepage := Project.homepage
ThisBuild / developers := Project.developers
ThisBuild / licenses := Project.licenses
ThisBuild / scmInfo := Project.scmInfo
ThisBuild / description := Project.description

lazy val root = (project in file("."))
  .enablePlugins(ScriptedPlugin)
  .settings(
    name := "sbt-bom",
    sbtPlugin := true,
    libraryDependencies ++= Dependencies.library,
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++ Seq(
        "-Xmx1024M",
        "-Dplugin.version=" + version.value,
        "-Dplugin.organization=" + organization.value
      )
    },
    scriptedBufferLog := false,
    dependencyOverrides += "org.typelevel" %% "jawn-parser" % "0.14.1"
  )

ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true
