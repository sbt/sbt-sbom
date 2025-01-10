// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

package com.github.sbt.sbom.model

import org.cyclonedx.model.Component.{ Scope, Type }

final case class Module(
    group: String,
    name: String,
    version: String,
    modified: Boolean,
    componentType: Type,
    componentScope: Scope,
    licenses: Seq[License]
)
