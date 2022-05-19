package net.bstjohn.ad.generator.format.domains

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.bstjohn.ad.generator.format.common.Meta

case class Domains(
  data: Iterable[Domain],
  meta: Meta
)

object Domains {
  implicit val DomainsDecoder: Decoder[Domains] = deriveDecoder[Domains]
  implicit val DomainsEncoder: Encoder[Domains] = deriveEncoder[Domains]

}