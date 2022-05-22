package net.bstjohn.ad.generator.format.ous

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json, JsonObject}
import net.bstjohn.ad.generator.format.ace.Ace

case class Ou(
  Links: List[Json],
  ChildObjects: List[Json],
  Aces: Iterable[Ace],
  ObjectIdentifier: String,
  IsDeleted: Boolean,
  IsACLProtected: Boolean,
  GPOChanges: JsonObject,
  Properties: OuProperties,
)

object Ou {
  implicit val OuDecoder: Decoder[Ou] = deriveDecoder[Ou]
  implicit val OuEncoder: Encoder[Ou] = deriveEncoder[Ou]


}