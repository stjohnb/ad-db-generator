package net.bstjohn.ad.generator.format.containers

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json}
import net.bstjohn.ad.generator.format.ace.Ace

case class Container(
  ChildObjects: List[Json],
  Aces: Seq[Ace],
  ObjectIdentifier: String,
  IsDeleted: Boolean,
  IsACLProtected: Boolean,
  Properties: ContainerProperties,
)

object Container {
  implicit val ContainerDecoder: Decoder[Container] = deriveDecoder[Container]
  implicit val ContainerEncoder: Encoder[Container] = deriveEncoder[Container]


}