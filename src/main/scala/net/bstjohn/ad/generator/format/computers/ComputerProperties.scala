package net.bstjohn.ad.generator.format.computers

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json}

case class ComputerProperties(
  domain: String,
  name: String,
  distinguishedname: Option[String],
  domainsid: String,
  highvalue: Option[Boolean],
  haslaps: Boolean,
  description: Option[String],
  whencreated: Option[Long],
  unconstraineddelegation: Option[Boolean],
  trustedtoauth: Option[Boolean],
  lastlogon: Option[Long],
  lastlogontimestamp: Option[Long],
  enabled: Option[Boolean],
  pwdlastset: Option[Long],
  serviceprincipalnames: Option[List[String]],
  operatingsystem: Option[String],
  sidhistory: Option[List[Json]],
)

object ComputerProperties {
  implicit val ComputerPropertiesDecoder: Decoder[ComputerProperties] = deriveDecoder[ComputerProperties]
  implicit val ComputerPropertiesEncoder: Encoder[ComputerProperties] = deriveEncoder[ComputerProperties].mapJson(_.deepDropNullValues)

}
