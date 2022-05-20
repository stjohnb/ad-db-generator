package net.bstjohn.ad.generator.format.users

import com.softwaremill.diffx.Diff
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json}
import net.bstjohn.ad.generator.snapshots.diffs.JsonDiffInstance

case class UserProperties(
  domain: String,
  name: String,
  distinguishedname: Option[String],
  domainsid: String,
  highvalue: Option[Boolean],
  description: Option[String],
  whencreated: Option[Int],
  sensitive: Option[Boolean],
  dontreqpreauth: Option[Boolean],
  passwordnotreqd: Option[Boolean],
  unconstraineddelegation: Option[Boolean],
  pwdneverexpires: Option[Boolean],
  enabled: Option[Boolean],
  trustedtoauth: Option[Boolean],
  lastlogon: Option[Int],
  lastlogontimestamp: Option[Int],
  pwdlastset: Option[Int],
  serviceprincipalnames: Option[List[Json]],
  hasspn: Option[Boolean],
  displayname: Option[Json],
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

  import JsonDiffInstance._

  implicit val jsonOptionDiffInstance: Diff[Option[Json]] = JsonDiffInstance.diffForOptionJson(implicitly[Diff[String]])
  implicit val jsonOptionListDiffInstance: Diff[Option[List[Json]]] = JsonDiffInstance.diffForOptionListJson(implicitly[Diff[String]])

  implicit val UserPropertiesDiff = Diff.derived[UserProperties]

}
