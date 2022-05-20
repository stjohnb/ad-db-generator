package net.bstjohn.ad.generator.format.ace

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class Ace(
  PrincipalSID: String,
  PrincipalType: AcePrincipalType,
  RightName: RightName,
  IsInherited: Boolean
)


object Ace {
  implicit val AceDecoder: Decoder[Ace] = deriveDecoder[Ace]
  implicit val AceEncoder: Encoder[Ace] = deriveEncoder[Ace]

}