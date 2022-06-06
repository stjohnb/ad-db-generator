package net.bstjohn.ad.generator.format.computers

import com.softwaremill.diffx.Diff
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json}
import net.bstjohn.ad.preprocessing.diffs.JsonDiffInstance

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

  implicit val jsonOptionDiffInstance: Diff[Option[Json]] = JsonDiffInstance.diffForOptionJson(implicitly[Diff[String]])
  implicit val jsonOptionListDiffInstance: Diff[Option[List[Json]]] = JsonDiffInstance.diffForOptionListJson(implicitly[Diff[String]])

  implicit val ComputerPropertiesDiff = Diff.derived[ComputerProperties]

}
