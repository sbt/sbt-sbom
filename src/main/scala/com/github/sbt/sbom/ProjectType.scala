// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

package com.github.sbt.sbom

sealed trait ProjectType
  case object APPLICATION extends ProjectType
  case object FRAMEWORK extends ProjectType
  case object LIBRARY extends ProjectType
  case object CONTAINER extends ProjectType
  case object PLATFORM extends ProjectType
  case object OPERATING_SYSTEM extends ProjectType
  case object DEVICE extends ProjectType
  case object DEVICE_DRIVER extends ProjectType
  case object FIRMWARE extends ProjectType
  case object FILE extends ProjectType
  case object MACHINE_LEARNING_MODEL extends ProjectType
  case object DATA extends ProjectType
  case object CRYPTOGRAPHIC_ASSET extends ProjectType

object ProjectType {
  def fromString(t: String): ProjectType = {
    val projType = t.trim().toUpperCase().replace("-", "_")

    Vector(APPLICATION, FRAMEWORK, LIBRARY, CONTAINER, PLATFORM, OPERATING_SYSTEM,
      DEVICE, DEVICE_DRIVER, FIRMWARE, FILE, MACHINE_LEARNING_MODEL, DATA, CRYPTOGRAPHIC_ASSET).find(_.toString == projType)
      .getOrElse(throw new ClassNotFoundException(
        s"Given Project Type not found. Refer to ${ProjectType.getClass.getName} or the list of available type.")
      )
  }
}
