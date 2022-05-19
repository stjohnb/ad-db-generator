package net.bstjohn.ad.generator.format.domains

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json, JsonObject}
import net.bstjohn.ad.generator.format.common.Ace

case class Domain(
  ChildObjects: List[DomainChild],
  Trusts: List[Json],
  Links: List[JsonObject],
  Aces: List[Ace],
  ObjectIdentifier: String,
  IsDeleted: Boolean,
  IsACLProtected: Boolean,
  GPOChanges: JsonObject,
  Properties: DomainProperties,
)

object Domain {
  implicit val DomainDecoder: Decoder[Domain] = deriveDecoder[Domain]
  implicit val DomainEncoder: Encoder[Domain] = deriveEncoder[Domain]

}