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

// sbt 2.x includes the projectMatrix plugin. We must define the plugin
// only for sbt 1.x (which uses Scala 2.12.x)
if (scala.util.Properties.versionNumberString.startsWith("2.12."))
  addSbtPlugin("com.eed3si9n" % "sbt-projectmatrix" % "0.11.0")
else
  Seq.empty[Setting[?]]
