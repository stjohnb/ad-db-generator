package net.bstjohn.ad.generator.format.common

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class Ace(
  PrincipalSID: String,
  PrincipalType: AcePrincipalType,
  RightName: String,
  IsInherited: Boolean
)


object Ace {
  implicit val AceDecoder: Decoder[Ace] = deriveDecoder[Ace]
  implicit val AceEncoder: Encoder[Ace] = deriveEncoder[Ace]

}