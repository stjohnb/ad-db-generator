package net.bstjohn.ad.generator.format.computers

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.bstjohn.ad.generator.format.common.{Meta, MetaType}

case class Computers(
  data: Seq[Computer],
  meta: Meta
)

object Computers {
  implicit val ComputersDecoder: Decoder[Computers] = deriveDecoder[Computers]
  implicit val ComputersEncoder: Encoder[Computers] = deriveEncoder[Computers].mapJson(_.deepDropNullValues)

  def apply(computers: Seq[Computer]): Computers = {
    Computers(computers, Meta(MetaType.computers, computers.size))
  }
}