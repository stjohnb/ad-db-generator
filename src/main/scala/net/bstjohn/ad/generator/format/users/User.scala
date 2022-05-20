package net.bstjohn.ad.generator.format.users

import com.softwaremill.diffx.Diff
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json, JsonObject}
import net.bstjohn.ad.generator.format.common.Ace

case class User(
  AllowedToDelegate: List[String],
  PrimaryGroupSID: Option[String],
  HasSIDHistory: List[String],
  SPNTargets: List[String],
  Aces: List[Ace],
  ObjectIdentifier: String,
  IsDeleted: Boolean,
  IsACLProtected: Boolean,
  Properties: UserProperties,
)

object User {
  implicit val UserDecoder: Decoder[User] = deriveDecoder[User]
  implicit val UserEncoder: Encoder[User] = deriveEncoder[User]


}