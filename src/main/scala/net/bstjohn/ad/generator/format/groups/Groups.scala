package net.bstjohn.ad.generator.format.groups

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.bstjohn.ad.generator.format.common.Meta

case class Groups(
  data: List[Group],
  meta: Meta
)

object Groups {
  implicit val GroupsDecoder: Decoder[Groups] = deriveDecoder[Groups]
  implicit val GroupsEncoder: Encoder[Groups] = deriveEncoder[Groups]

}