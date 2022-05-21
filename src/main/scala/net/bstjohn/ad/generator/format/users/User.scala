package net.bstjohn.ad.generator.format.users

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.bstjohn.ad.generator.format.ace.Ace
import net.bstjohn.ad.generator.generators.model.EpochSeconds

import java.util.Date

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
) {
  def loggedOn(timestamp: EpochSeconds): User = {
    copy(Properties = Properties.copy(
      lastlogon = Some(timestamp.value),
      lastlogontimestamp = Some(timestamp.value)
    ))
  }
}

object User {
  implicit val UserDecoder: Decoder[User] = deriveDecoder[User]
  implicit val UserEncoder: Encoder[User] = deriveEncoder[User]


}