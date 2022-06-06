package net.bstjohn.ad.generator.format.gpos

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json}
import net.bstjohn.ad.generator.format.common.{Meta, MetaType}

case class Gpos(
  data: Seq[Gpo],
  meta: Meta
)

object Gpos {
  implicit val GposDecoder: Decoder[Gpos] = deriveDecoder[Gpos]
  implicit val GposEncoder: Encoder[Gpos] = deriveEncoder[Gpos].mapJson(_.deepDropNullValues)

  def apply(containers: Seq[Gpo]): Gpos = {
    Gpos(containers, Meta(MetaType.gpos, containers.size))
  }
}