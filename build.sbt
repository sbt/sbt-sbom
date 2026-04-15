// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

ThisBuild / organization := Organization.organization
ThisBuild / organizationName := Organization.organizationName
ThisBuild / organizationHomepage := Organization.organizationHomepage
val scala212 = "2.12.21"
val scala3 = "3.8.2"
ThisBuild / scalaVersion := scala212
ThisBuild / crossScalaVersions := Seq(scala212, scala3)
ThisBuild / homepage := Project.homepage
ThisBuild / developers := Project.developers
ThisBuild / licenses := Project.licenses
ThisBuild / scmInfo := Project.scmInfo
ThisBuild / description := Project.description

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin, ScriptedPlugin, BuildInfoPlugin)
  .settings(
    name := "sbt-sbom",
    libraryDependencies ++= Dependencies.library,
    // Explicit Maven artifact IDs so sbt 1.6 scripted resolution requests
    // sbt2-compat_2.12_1.0-0.1.0.pom (addSbtPlugin can request sbt2-compat-0.1.0.pom, which 404s).
    libraryDependencies += (scalaBinaryVersion.value match {
      case "2.12" => "com.github.sbt" % "sbt2-compat_2.12_1.0" % "0.1.0"
      case _      => "com.github.sbt" % "sbt2-compat_sbt2_3"   % "0.1.0"
    }),
    buildInfoPackage := "com.github.sbt.sbom",
    (pluginCrossBuild / sbtVersion) := (scalaBinaryVersion.value match {
      case "2.12" => "1.10.7"
      case _      => "2.0.0-RC11"
    }),
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++ Seq(
        "-Xmx1024M",
        "-Dplugin.version=" + version.value,
        "-Dplugin.organization=" + organization.value
      )
    },
    scriptedBufferLog := false,
    scriptedSbt := (scalaBinaryVersion.value match {
      case "2.12" => "1.10.11"
      case _      => "2.0.0-RC11"
    }),
    scalacOptions ++= {
      scalaBinaryVersion.value match {
        case "2.12" => Seq("-Ywarn-unused", "-release:17")
        case _      => Seq("-Wunused:all")
      }
    }
  )

ThisBuild / pomIncludeRepository := { _ =>
  false
}
ThisBuild / publishMavenStyle := true

ThisBuild / githubWorkflowBuildPreamble := Seq(
  WorkflowStep.Sbt(List("scalafixAll --check"), name = Some("Linter: Scalafix checks"))
)

ThisBuild / githubWorkflowBuild := Seq(WorkflowStep.Sbt(List("test", "scripted")))

ThisBuild / githubWorkflowArtifactUpload := false

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches :=
  Seq(
    RefPredicate.StartsWith(Ref.Tag("v")),
    RefPredicate.Equals(Ref.Branch("main"))
  )
ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    commands = List("ci-release"),
    name = Some("Publish project"),
    env = Map(
      "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)

ThisBuild / githubWorkflowOSes := Seq("ubuntu-latest", "macos-latest", "windows-latest")

ThisBuild / githubWorkflowScalaVersions := Seq(scala212, scala3)

ThisBuild / githubWorkflowJavaVersions := Seq(
  JavaSpec.temurin("17"),
  JavaSpec.temurin("25"),
)

// Semantic DB (used by scalafix)
inThisBuild(
  List(
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)
