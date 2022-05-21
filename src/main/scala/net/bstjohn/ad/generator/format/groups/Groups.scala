package net.bstjohn.ad.generator.format.groups

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.bstjohn.ad.generator.format.common.{Meta, MetaType}

case class Groups(
  data: Iterable[Group],
  meta: Meta
)

object Groups {
  implicit val GroupsDecoder: Decoder[Groups] = deriveDecoder[Groups]
  implicit val GroupsEncoder: Encoder[Groups] = deriveEncoder[Groups].mapJson(_.deepDropNullValues)

  def apply(groups: Iterable[Group]): Groups = {
    Groups(
      data = groups,
      meta = Meta(
        methods = 29675,
        `type` = MetaType.groups,
        count = groups.size,
        version = 4
      )
    )
  }

}