# sbt-sbom

*sbt SBOM exporter*

The aim of this [project](https://github.com/sbt/sbt-sbom/) is to:

- extract a valid [CycloneDx](https://cyclonedx.org/) bom file from [sbt](https://www.scala-sbt.org/) projects
- ensure that the bom file is processable with Software Composition Analysis tools (like [Dependency Track](https://dependencytrack.org/))

Current version of the plugin is 0.3.0, published to the Central Repository.

Snapshot version are published to the [Sonatype Repository](https://s01.oss.sonatype.org/content/repositories/snapshots).

## usage

### project setup

Add the plugin dependency to the file `project/plugins.sbt` using `addSbtPlugin` :

`addSbtPlugin("io.github.siculo" %% "sbt-bom" % "0.3.0")`

Note that the minimum supported version of sbt is 1.5.2 (this is what the [scripted](https://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html#scripted+test+framework) tests target)

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

### scalafix

This project uses [scalafix](https://scalacenter.github.io/scalafix/) as a linter/style guide enforcer. To run scalafix
you can simply do

```sbt
scalafixAll
```

## changelog

### v0.4.0

### v0.3.0
- The BOM is generated so that it takes into account the Scope (Compile, Test...) and its dependencies
- targetBomFile setting replaced by bomFileName
- default BOM file name is ${artifactId}-${version}.bom.xml
- GroupId has been changed to io.github.siculo
- Generated BOM is a valid 1.0 BOM file (removed unespected properties like BOM serial number and license URL)

### v0.2.0
- The cyclonedx-core-java library has been integrated and is used to generate the BOM
- Removed all old model classes used so far

### v0.1.0
- First release
