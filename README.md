<!--
SPDX-FileCopyrightText: The sbt-sbom team

SPDX-License-Identifier: MIT
-->

# sbt-sbom

*sbt SBOM exporter*

The aim of this [project](https://github.com/sbt/sbt-sbom/) is to:

- extract a valid [CycloneDx](https://cyclonedx.org/) bom file from [sbt](https://www.scala-sbt.org/) projects
- ensure that the bom file is processable with Software Composition Analysis tools (like [Dependency Track](https://dependencytrack.org/))

Current version of the plugin is 0.4.0, published to the Central Repository.

Snapshot version are published to the [Sonatype Repository](https://s01.oss.sonatype.org/content/repositories/snapshots).

## usage

### project setup

Add the plugin dependency to the file `project/plugins.sbt` using `addSbtPlugin` :

`addSbtPlugin("com.github.sbt" %% "sbt-sbom" % "0.4.0")`

Note that the minimum supported version of sbt is 1.5.2 (this is what the [scripted](https://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html#scripted+test+framework) tests target)

### BOM creation

To create the bom for the default configuration use `makeBom` command:

`> sbt makeBom`

This creates the BOM file inside the `target` directory. The name of the file created depends on the `name` and `version` property of the current project. For example, if name and version are `myArtifact` and `1.0`, the file name is `myArtifact-1.0.bom.json`.

### scope selection

It is possible to create the BOM for different scopes, so that all dependencies of the scopes are included in the generated BOM files. The default scope is `Compile`. For now the other supported scopes are `Test` and `IntegrationTest`. To generate the BOM for a certain scope, add the scope as a prefix to the `makeBom` command:

`> sbt Test / makeBom`

`> sbt IntegrationTest / makeBom`

### listing BOM content

The `listBom` command can be used to generate the contents of the BOM without writing it to a file. The BOM is returned as command output. To display the BOM content use: 

`> sbt show listBom`

### configuration

| Setting                      | Type    | Default                                                                | Description                                                     |
|------------------------------|---------|------------------------------------------------------------------------|-----------------------------------------------------------------|
| bomFileName                  | String  | `"${artifactId}-${artifactVersion}.bom.json"`                          | bom file name                                                   |
| bomFormat                    | String  | `json` or `xml`, defaults to the format of bomFileName or else `json`  | bom format                                                      |
| bomSchemaVersion             | String  | `"1.6"`                                                                | bom schema version                                              |
| includeBomSerialNumber       | Boolean | `false`                                                                | include serial number in bom                                    |
| includeBomTimestamp          | Boolean | `false`                                                                | include timestamp in bom                                        |
| includeBomToolVersion        | Boolean | `true`                                                                 | include tool version in bom                                     |
| includeBomHashes             | Boolean | `true`                                                                 | include artifact hashes in bom                                  |
| enableBomSha3Hashes          | Boolean | `true`                                                                 | enable the generation of sha3 hashes (not available on java 8)  |
| includeBomExternalReferences | Boolean | `true`                                                                 | include external references in bom                              |
| includeBomDependencyTree     | Boolean | `true`                                                                 | include dependency tree in bom (bomSchemaVersion 1.1 and later) |

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

This plugin supports the CycloneDX XML and JSON BOM formats.

## Stability

We believe this plugin is stable enough to be used in production, but
we do not yet promise API stability: you may need to make configuration
changes or encounter changed behaviour when updating the plugin.

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

Scripted tests are run using `scripted` command. Note that these fail on JDK 21 due to the old version of sbt.

### Formatting

The codebase is formatted with [scalafmt](https://scalameta.org/scalafmt/), as such the codebase needs to be formatted
before submitting a PR.

Various runners for Scalafmt exist, such as
* A [sbt scalafmt plugin](https://github.com/scalameta/sbt-scalafmt) that lets you run scalafmt directly within sbt using
    * `scalafmt` to format base scala sources
    * `test:scalafmt` to format test scala sources
    * `scalafmtSbt` to format the `build.sbt` file
    * `scalafmtAll` to format everything
* IntelliJ IDEA and VSCode will automatically detect projects with scalafmt and prompt you whether to use Scalafmt. See
  the [scalafmt installation guide][scalafmt-installation-link] for more details
* There are native builds of Scalafmt that let you run a `scalafmt` as a CLI tool, see the CLI section in
  [scalafmt installation guide][scalafmt-installation-link]

Note that a [GitHub action exists](https://github.com/sbt/sbt-sbom/blob/main/.github/workflows/format.yml) which will
check that your code is formatted whenever you create a PR.

### Linting

This project uses [scalafix](https://scalacenter.github.io/scalafix/) as a linter/style guide enforcer. To run scalafix
you can simply do

```sbt
clean test/clean scalafixAll
```

Note that its possible that running scalafix may generate code that isn't compliant with scalafmt so it's
a good idea to [run scalafmt](#formatting) on the code afterward

## changelog

### v0.4.0

- Generate the latest supported CycloneDX version (1.6)
- Default BOM file name is `${artifactId}-${version}.bom.json`
- GroupId has been changed to `com.github.sbt`
- SBOM extractor improvements

### v0.3.0
- The BOM is generated so that it takes into account the Scope (Compile, Test...) and its dependencies
- targetBomFile setting replaced by bomFileName
- default BOM file name is ${artifactId}-${version}.bom.xml
- GroupId has been changed to io.github.siculo
- Generated BOM is a valid 1.0 BOM file (removed unexpected properties like BOM serial number and license URL)

### v0.2.0
- The cyclonedx-core-java library has been integrated and is used to generate the BOM
- Removed all old model classes used so far

### v0.1.0
- First release

[scalafmt-installation-link]: https://scalameta.org/scalafmt/docs/installation.html
