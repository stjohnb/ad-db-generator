package net.bstjohn.ad.generator.format.groups

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.bstjohn.ad.generator.format.common.{Meta, MetaType}
import net.bstjohn.ad.generator.generators.entities.GroupGenerator

case class Groups(
  data: Seq[Group],
  meta: Meta
) {
  def domainAdminsGroup: Option[Group] = data.find(_.Properties.name == GroupGenerator.DomainAdminsGroupName)
}

object Groups {
  implicit val GroupsDecoder: Decoder[Groups] = deriveDecoder[Groups]
  implicit val GroupsEncoder: Encoder[Groups] = deriveEncoder[Groups].mapJson(_.deepDropNullValues)

  def apply(groups: Seq[Group]): Groups = {
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