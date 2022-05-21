package net.bstjohn.ad.generator.format.common

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class Meta(
  methods: Int,
  `type`: MetaType,
  count: Int,
  version: Int
)

object Meta {
  implicit val MetaDecoder: Decoder[Meta] = deriveDecoder[Meta]
  implicit val MetaEncoder: Encoder[Meta] = deriveEncoder[Meta]

  def apply(`type`: MetaType, count: Int): Meta = {
    Meta(
      methods = 29675,
      `type` = `type`,
      count = count,
      version = 4
    )

  }
}