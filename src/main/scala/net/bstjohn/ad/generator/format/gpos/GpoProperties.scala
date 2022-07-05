package net.bstjohn.ad.generator.format.gpos

import com.softwaremill.diffx.Diff
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

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

  implicit val GpoPropertiesDiff = Diff.derived[GpoProperties]

}
