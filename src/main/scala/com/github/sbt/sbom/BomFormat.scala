// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

package com.github.sbt.sbom

import com.github.sbt.sbom.PluginConstants.supportedVersions
import org.cyclonedx.Version

sealed abstract class BomFormat(val string: String)

object BomFormat {
  case object Json extends BomFormat("json")
  case object Xml extends BomFormat("xml")

  def fromSettings(bomFormat: Option[String], bomFileName: Option[String], schemaVersion: String): BomFormat = {
    bomFormat
      .collect {
        case Json.string => Json
        case Xml.string  => Xml
        case format      =>
          throw new BomError(s"Unsupported format ${format}")
      }
      .orElse {
        bomFileName.map(_.toLowerCase).collect {
          case ext if ext.endsWith(".json") => Json
          case ext if ext.endsWith(".xml")  => Xml
        }
      }
      .orElse {
        supportedVersions.find(_.getVersionString == schemaVersion).collect {
          case foundVersion if foundVersion.getVersion > Version.VERSION_11.getVersion => Json
        }
      }
      .getOrElse {
        Xml
      }
  }
}
