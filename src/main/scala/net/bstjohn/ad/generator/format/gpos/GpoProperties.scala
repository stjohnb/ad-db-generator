package net.bstjohn.ad.generator.format.gpos

import com.softwaremill.diffx.Diff
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json}
import net.bstjohn.ad.preprocessing.diffs.JsonDiffInstance

case class GpoProperties(
  domain: String,
  name: String,
  distinguishedname: Option[String],
  domainsid: String,
  highvalue: Option[Boolean],
  description: Option[String],
  whencreated: Option[Long],
  gpcpath: String
)

object GpoProperties {
  implicit val GpoPropertiesDecoder: Decoder[GpoProperties] = deriveDecoder[GpoProperties]
  implicit val GpoPropertiesEncoder: Encoder[GpoProperties] = deriveEncoder[GpoProperties].mapJson(_.deepDropNullValues)

  implicit val jsonOptionDiffInstance: Diff[Option[Json]] = JsonDiffInstance.diffForOptionJson(implicitly[Diff[String]])
  implicit val jsonOptionListDiffInstance: Diff[Option[List[Json]]] = JsonDiffInstance.diffForOptionListJson(implicitly[Diff[String]])

  implicit val GpoPropertiesDiff = Diff.derived[GpoProperties]

}
