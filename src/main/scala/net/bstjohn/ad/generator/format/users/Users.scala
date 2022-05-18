package net.bstjohn.ad.generator.format.users

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.bstjohn.ad.generator.format.common.Meta

case class Users(
  data: Iterable[User],
  meta: Meta
)

object Users {
  implicit val UsersDecoder: Decoder[Users] = deriveDecoder[Users]
  implicit val UsersEncoder: Encoder[Users] = deriveEncoder[Users]

}