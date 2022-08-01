package net.bstjohn.ad.generator.format.computers

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import net.bstjohn.ad.generator.format.common.EntityId.UserId

case class Session(
  UserSID: String,
  ComputerSID: String
) {
  def userId: UserId = UserId(UserSID)
}

object Session {


  implicit val SessionDecoder: Decoder[Session] = deriveDecoder[Session]
  implicit val SessionEncoder: Encoder[Session] = deriveEncoder[Session]

}
