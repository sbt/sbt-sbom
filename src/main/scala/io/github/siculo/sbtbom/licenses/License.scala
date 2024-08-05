package io.github.siculo.sbtbom.licenses

case class License(
                    id: String,
                    name: String,
                    references: Seq[String]
                  )
