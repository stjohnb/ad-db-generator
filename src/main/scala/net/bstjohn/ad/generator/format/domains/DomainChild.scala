package net.bstjohn.ad.generator.format.domains

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class DomainChild(
  ObjectIdentifier: String,
  ObjectType: DomainChildType,
)

object DomainChild {
  implicit val DomainChildDecoder: Decoder[DomainChild] = deriveDecoder[DomainChild]
  implicit val DomainChildEncoder: Encoder[DomainChild] = deriveEncoder[DomainChild]

}
