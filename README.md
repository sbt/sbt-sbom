# sbt-bom

[![build](https://github.com/lhns/sbt-bom/actions/workflows/build.yml/badge.svg)](https://github.com/lhns/sbt-bom/actions/workflows/build.yml)
[![Release Notes](https://img.shields.io/github/release/lhns/sbt-bom.svg?maxAge=3600)](https://github.com/lhns/sbt-bom/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/de.lhns/sbt-bom_2.12_1.0)](https://search.maven.org/artifact/de.lhns/sbt-bom_2.12_1.0)
[![Apache License 2.0](https://img.shields.io/github/license/lhns/sbt-bom.svg?maxAge=3600)](https://opensource.org/license/mit)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

*sbt bom.xml exporter*

The aim of this [project](https://siculo.github.io/sbt-bom/) is to:

- extract a valid [CycloneDx](https://cyclonedx.org/) bom file from [sbt](https://www.scala-sbt.org/) projects
- ensure that the bom file is processable with Software Composition Analysis tools (like [Dependency Track](https://dependencytrack.org/))

## usage

### project setup

Add the plugin dependency to the file `project/plugins.sbt` using `addSbtPlugin` :

`addSbtPlugin("de.lhns" %% "sbt-bom" % "0.4.0")`

### BOM creation

To create the bom for the default configuration use `makeBom` command:

`> sbt makeBom`

This create the BOM file inside the `target` directory. The name of the file created depends on the `name` and `version` property of the current project. For example, if name and version are `myArtifact` and `1.0`, the file name is `myArtifact-1.0.bom.xml`.

### scope selection

It is possible to create the BOM for different scopes, so that all dependencies of the scopes are included in the generated BOM files. The default scope is `Compile`. For now the other supported scopes are `Test` and `IntegrationTest`. To generate the BOM for a certain scope, add the scope as a prefix to the `makeBom` command:

`> sbt Test / makeBom`

`> sbt IntegrationTest / makeBom`

### listing BOM content

The `listBom` command can be used to generate the contents of the BOM without writing it to a file. The BOM is returned as command output. To display the BOM content use: 

`> sbt show listBom`

### configuration

| Setting     | Type        | Description   |
| ----------- | ----------- | ------------- |
| bomFileName | String      | bom file name |

Sample configuration:

```scala
lazy val root = (project in file("."))
  .settings(
    bomFileName := "bom.xml",
    Test / bomFileName := "test.bom.xml",
    IntegrationTest / bomFileName := "integrationTest.bom.xml",
  )
```

## CycloneDX support

Actually, only version 1.0 of the CycloneDX specification is supported. Support for later versions of the specification, such as for creating BOMs in json format, is expected later.

## Contributing

### testing

There are two types of test: unit test done with scalatest and scripted test

### unit test

Unit tests are written using scalatest syntax. Only pure logic classes are tested using these tests.

To run unit tests use the `test` command to run all tests, or `testOnly ...` command specifying the list of test to be
executed.

### scripted tests

[Scripted](https://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html) is a tool that allow you to test sbt plugins.
For each test it is necessary to create a specially crafted project. These projects are inside src/sbt-test directory.

Scripted tests are run using `scripted` comand.

## changelog

See [Releases](https://github.com/lhns/sbt-bom/releases).

For versions before 0.4.0 see [siculo/sbt-bom#changelog](https://github.com/siculo/sbt-bom#changelog).
