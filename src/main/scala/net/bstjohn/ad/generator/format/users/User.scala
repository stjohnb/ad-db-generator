package net.bstjohn.ad.generator.format.users

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.bstjohn.ad.generator.format.ace.Ace
import net.bstjohn.ad.generator.format.common.EntityId.UserId
import net.bstjohn.ad.generator.generators.model.EpochSeconds

case class User(
  AllowedToDelegate: Seq[String],
  PrimaryGroupSID: Option[String],
  HasSIDHistory: Seq[String],
  SPNTargets: Seq[String],
  Aces: Seq[Ace],
  ObjectIdentifier: UserId,
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