package net.bstjohn.ad.generator.format.ous

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.bstjohn.ad.generator.format.common.{Meta, MetaType}

case class Ous(
  data: Seq[Ou],
  meta: Meta
)

object Ous {
  implicit val OusDecoder: Decoder[Ous] = deriveDecoder[Ous]
  implicit val OusEncoder: Encoder[Ous] = deriveEncoder[Ous].mapJson(_.deepDropNullValues)

  def apply(ous: Seq[Ou]): Ous = {
    Ous(ous, Meta(MetaType.ous, ous.size))
  }
}