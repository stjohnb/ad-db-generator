package net.bstjohn.ad.generator.format.containers

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json}
import net.bstjohn.ad.generator.format.common.{Meta, MetaType}

case class Containers(
  data: Iterable[Container],
  meta: Meta
)

object Containers {
  implicit val ContainersDecoder: Decoder[Containers] = deriveDecoder[Containers]
  implicit val ContainersEncoder: Encoder[Containers] = deriveEncoder[Containers].mapJson(_.deepDropNullValues)

  def apply(containers: Iterable[Container]): Containers = {
    Containers(containers, Meta(MetaType.containers, containers.size))
  }
}