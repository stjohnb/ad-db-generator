package net.bstjohn.ad.generator.format

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class Users(
  data: List[User],
  meta: Meta
)

object Users {
  implicit val UsersDecoder: Decoder[Users] = deriveDecoder[Users]
  implicit val UsersEncoder: Encoder[Users] = deriveEncoder[Users]

}