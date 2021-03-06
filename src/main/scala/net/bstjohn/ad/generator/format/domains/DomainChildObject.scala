package net.bstjohn.ad.generator.format.domains

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, JsonObject}

case class DomainChildObject(
  ObjectIdentifier: String,
  IsDeleted: Boolean,
  IsACLProtected: Boolean,
  GPOChanges: JsonObject,
  Properties: JsonObject,
)

object DomainChildObject {
  implicit val DomainChildObjectDecoder: Decoder[DomainChildObject] = deriveDecoder[DomainChildObject]
  implicit val DomainChildObjectEncoder: Encoder[DomainChildObject] = deriveEncoder[DomainChildObject]

}