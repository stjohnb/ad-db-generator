package net.bstjohn.ad.generator.format.containers

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class ContainerProperties(
  domain: String,
  name: String,
  distinguishedname: Option[String],
  domainsid: String,
  highvalue: Option[Boolean],
)

object ContainerProperties {
  implicit val ContainerPropertiesDecoder: Decoder[ContainerProperties] = deriveDecoder[ContainerProperties]
  implicit val ContainerPropertiesEncoder: Encoder[ContainerProperties] = deriveEncoder[ContainerProperties].mapJson(_.deepDropNullValues)

}
