// SPDX-FileCopyrightText: The sbt-sbom team
//
// SPDX-License-Identifier: MIT

(
  sys.props.get("plugin.version"),
  sys.props.get("plugin.organization")
) match {
  case (Some(version), Some(organization)) =>
    addSbtPlugin(organization % "sbt-sbom" % version)
  case (None, _) =>
    sys.error(
      """|The system property 'plugin.version' is not defined.
         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
    )
  case (_, None) =>
    sys.error(
      """|The system property 'plugin.organization' is not defined.
         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
    )
}
