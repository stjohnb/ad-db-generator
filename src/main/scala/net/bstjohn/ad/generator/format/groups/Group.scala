package net.bstjohn.ad.generator.format.groups

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.bstjohn.ad.generator.format.ace.Ace

case class Group(
  Members: Iterable[GroupMember],
  Aces: Iterable[Ace],
  ObjectIdentifier: String,
  IsDeleted: Boolean,
  IsACLProtected: Boolean,
  Properties: GroupProperties,
)

object Group {
  implicit val GroupDecoder: Decoder[Group] = deriveDecoder[Group]
  implicit val GroupEncoder: Encoder[Group] = deriveEncoder[Group]


}