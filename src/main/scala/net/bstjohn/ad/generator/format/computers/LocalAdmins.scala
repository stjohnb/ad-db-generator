package net.bstjohn.ad.generator.format.computers

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json}

case class LocalAdmins(
  Results: Option[List[LocalAdmin]],
  Collected: Option[Boolean],
  FailureReason: Option[Json]
) {
  def localAdmins: List[LocalAdmin] = Results.getOrElse(List.empty)
}

object LocalAdmins {
  implicit val LocalAdminsDecoder: Decoder[LocalAdmins] = deriveDecoder[LocalAdmins]
  implicit val LocalAdminsEncoder: Encoder[LocalAdmins] = deriveEncoder[LocalAdmins]

}
