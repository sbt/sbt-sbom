// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

package com.github.sbt.sbom.licenses

final case class License(id: String, name: String, references: Seq[String])
