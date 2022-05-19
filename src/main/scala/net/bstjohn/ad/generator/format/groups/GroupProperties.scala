package net.bstjohn.ad.generator.format.groups

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class GroupProperties(
  domain: String,
  name: String,
  distinguishedname: Option[String],
  domainsid: String,
  highvalue: Option[Boolean],
  description: Option[String],
  whencreated: Option[Int],
  admincount: Option[Boolean],
)


object GroupProperties {
  implicit val GroupPropertiesDecoder: Decoder[GroupProperties] = deriveDecoder[GroupProperties]
  implicit val GroupPropertiesEncoder: Encoder[GroupProperties] = deriveEncoder[GroupProperties].mapJson(_.deepDropNullValues)

}
