package net.bstjohn.ad.generator.format.common

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class Meta(
  methods: Int,
  `type`: String,
  count: Int,
  version: Int
)

object Meta {
  implicit val MetaDecoder: Decoder[Meta] = deriveDecoder[Meta]
  implicit val MetaEncoder: Encoder[Meta] = deriveEncoder[Meta]

}