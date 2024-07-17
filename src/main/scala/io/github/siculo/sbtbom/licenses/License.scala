package io.github.siculo.sbtbom.licenses

case class License(
                    id: Option[String],
                    name: Option[String],
                    references: Seq[String]
                  )
