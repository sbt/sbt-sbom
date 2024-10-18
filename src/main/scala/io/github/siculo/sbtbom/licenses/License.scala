package io.github.siculo.sbtbom.licenses

case class License(id: Option[String] = None, name: Option[String] = None, references: Seq[String] = Seq())
