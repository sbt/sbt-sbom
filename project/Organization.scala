// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

import sbt.{ URI, url }

object Organization {
  val organization: String = "com.github.sbt"
  val organizationName: String = "sbt"
  val organizationHomepage: Option[URI] = Some(url("https://www.scala-sbt.org/"))
}
