// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

import sbt.*

object Project {
  lazy val description: String =
    "SBT plugin to generate CycloneDx SBOM files"

  lazy val homepage: Option[URL] =
    Some(url("https://github.com/siculo/sbt-bom"))

  lazy val developers: List[Developer] =
    List(
      Developer("siculo", "Fabrizio Di Giuseppe", "siculo.github@gmail.com", url("https://github.com/siculo"))
    )

  lazy val licenses: List[(String, URL)] =
    List(
      ("MIT License", url("https://opensource.org/licenses/MIT"))
    )

  lazy val scmInfo: Option[ScmInfo] =
    Some(
      ScmInfo(
        url("https://github.com/siculo/sbt-bom/tree/master"),
        "scm:git:git://github.com/siculo/sbt-bom.git",
        Some("scm:git:ssh://github.com:siculo/sbt-bom.git")
      )
    )
}
