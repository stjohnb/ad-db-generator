package net.bstjohn.ad.generator.format.users

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json}

case class UserProperties(
  domain: String,
  name: String,
  distinguishedname: Option[String],
  domainsid: String,
  highvalue: Option[Boolean],
  description: Option[String],
  whencreated: Option[Long],
  sensitive: Option[Boolean],
  dontreqpreauth: Option[Boolean],
  passwordnotreqd: Option[Boolean],
  unconstraineddelegation: Option[Boolean],
  pwdneverexpires: Option[Boolean],
  enabled: Option[Boolean],
  trustedtoauth: Option[Boolean],
  lastlogon: Option[Long],
  lastlogontimestamp: Option[Long],
  pwdlastset: Option[Long],
  serviceprincipalnames: Option[List[String]],
  hasspn: Option[Boolean],
  displayname: Option[String],
  email: Option[Json],
  title: Option[Json],
  homedirectory: Option[Json],
  userpassword: Option[Json],
  unixpassword: Option[Json],
  unicodepassword: Option[Json],
  sfupassword: Option[Json],
  admincount: Option[Boolean],
  sidhistory: Option[List[Json]],
)

object UserProperties {
  implicit val UserPropertiesDecoder: Decoder[UserProperties] = deriveDecoder[UserProperties]
  implicit val UserPropertiesEncoder: Encoder[UserProperties] = deriveEncoder[UserProperties].mapJson(_.deepDropNullValues)

}
