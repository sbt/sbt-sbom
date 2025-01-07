// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

package com.github.sbt.sbom

class ListBomTask(properties: BomTaskProperties) extends BomTask[String](properties) {
  override def execute: String = {
    log.info("Creating bom")
    val bomText = getBomText
    log.info("Bom created")
    bomText
  }
}
