package net.bstjohn.ad.generator.format.computers

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import net.bstjohn.ad.generator.format.common.EntityId.UserId

case class Session(
  UserSID: UserId,
  ComputerSID: String
)

object Session {


  implicit val SessionDecoder: Decoder[Session] = deriveDecoder[Session]
  implicit val SessionEncoder: Encoder[Session] = deriveEncoder[Session]

}
