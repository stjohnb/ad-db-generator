package net.bstjohn.ad.generator.format.containers

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.bstjohn.ad.generator.format.common.{Meta, MetaType}

case class Containers(
  data: Seq[Container],
  meta: Meta
)

object Containers {
  implicit val ContainersDecoder: Decoder[Containers] = deriveDecoder[Containers]
  implicit val ContainersEncoder: Encoder[Containers] = deriveEncoder[Containers].mapJson(_.deepDropNullValues)

  def apply(containers: Seq[Container]): Containers = {
    Containers(containers, Meta(MetaType.containers, containers.size))
  }
}