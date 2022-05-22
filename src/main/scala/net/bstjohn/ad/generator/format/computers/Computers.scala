package net.bstjohn.ad.generator.format.computers

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.bstjohn.ad.generator.format.common.{Meta, MetaType}

case class Computers(
  data: Iterable[Computer],
  meta: Meta
)

object Computers {
  implicit val ComputersDecoder: Decoder[Computers] = deriveDecoder[Computers]
  implicit val ComputersEncoder: Encoder[Computers] = deriveEncoder[Computers].mapJson(_.deepDropNullValues)

  def apply(users: Iterable[Computer]): Computers = {
    Computers(users, Meta(MetaType.users, users.size))
  }
}