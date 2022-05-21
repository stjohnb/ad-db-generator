package net.bstjohn.ad.generator.format.domains

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.bstjohn.ad.generator.format.common.{Meta, MetaType}

case class Domains(
  data: Iterable[Domain],
  meta: Meta
)

object Domains {

  def apply(domains: Iterable[Domain]): Domains = {
    Domains(
      data = domains,
      meta = Meta(
        methods = 29675,
        `type` = MetaType.domains,
        count = domains.size,
        version = 4
      )
    )
  }

  implicit val DomainsDecoder: Decoder[Domains] = deriveDecoder[Domains]
  implicit val DomainsEncoder: Encoder[Domains] = deriveEncoder[Domains]

}