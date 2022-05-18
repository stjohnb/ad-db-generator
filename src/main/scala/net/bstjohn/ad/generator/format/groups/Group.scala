package net.bstjohn.ad.generator.format.groups

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, JsonObject}
import net.bstjohn.ad.generator.format.common.Ace

case class Group(
  Members: Iterable[GroupMember],
  Aces: Iterable[Ace],
  ObjectIdentifier: String,
  IsDeleted: Boolean,
  IsACLProtected: Boolean,
  Properties: JsonObject,
)

object Group {
  implicit val GroupDecoder: Decoder[Group] = deriveDecoder[Group]
  implicit val GroupEncoder: Encoder[Group] = deriveEncoder[Group]


}