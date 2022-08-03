package net.bstjohn.ad.generator.format.users

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.bstjohn.ad.generator.format.common.{Meta, MetaType}

case class Users(
  data: Seq[User],
  meta: Meta
)

object Users {
  implicit val UsersDecoder: Decoder[Users] = deriveDecoder[Users]
  implicit val UsersEncoder: Encoder[Users] = deriveEncoder[Users].mapJson(_.deepDropNullValues)

  val Empty = Users(Seq.empty)

  def apply(users: Seq[User]): Users = {
    Users(users, Meta(MetaType.users, users.size))
  }
}