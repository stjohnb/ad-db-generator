package net.bstjohn.ad.generator.format.domains

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class DomainProperties(
  domain: String,
  name: String,
  distinguishedname: String,
  domainsid: String,
  highvalue: Boolean,
  description: Option[String],
  whencreated: Int,
  functionallevel: String,
)

object DomainProperties {
  implicit val DomainPropertiesDecoder: Decoder[DomainProperties] = deriveDecoder[DomainProperties]
  implicit val DomainPropertiesEncoder: Encoder[DomainProperties] = deriveEncoder[DomainProperties]
}
