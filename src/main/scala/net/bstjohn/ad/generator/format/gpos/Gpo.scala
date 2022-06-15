package net.bstjohn.ad.generator.format.gpos

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.bstjohn.ad.generator.format.ace.Ace

case class Gpo(
  Aces: Seq[Ace],
  ObjectIdentifier: String,
  IsDeleted: Boolean,
  IsACLProtected: Boolean,
  Properties: GpoProperties,
)

object Gpo {
  implicit val GpoDecoder: Decoder[Gpo] = deriveDecoder[Gpo]
  implicit val GpoEncoder: Encoder[Gpo] = deriveEncoder[Gpo]


}