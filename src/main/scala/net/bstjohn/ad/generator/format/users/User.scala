package net.bstjohn.ad.generator.format.users

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.bstjohn.ad.generator.format.common.Ace

case class User(
  AllowedToDelegate: Iterable[String],
  PrimaryGroupSID: Option[String],
  HasSIDHistory: Iterable[String],
  SPNTargets: Iterable[String],
  Aces: Iterable[Ace],
  ObjectIdentifier: String,
  IsDeleted: Boolean,
  IsACLProtected: Boolean,
  Properties: UserProperties,
)

object User {
  implicit val UserDecoder: Decoder[User] = deriveDecoder[User]
  implicit val UserEncoder: Encoder[User] = deriveEncoder[User]


}