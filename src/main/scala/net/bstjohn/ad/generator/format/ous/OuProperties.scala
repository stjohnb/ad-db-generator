package net.bstjohn.ad.generator.format.ous

import com.softwaremill.diffx.Diff
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class OuProperties(
  domain: String,
  name: String,
  distinguishedname: Option[String],
  domainsid: String,
  highvalue: Option[Boolean],
  description: Option[String],
  whencreated: Option[Long],
  blocksinheritance: Boolean
)

object OuProperties {
  implicit val OuPropertiesDecoder: Decoder[OuProperties] = deriveDecoder[OuProperties]
  implicit val OuPropertiesEncoder: Encoder[OuProperties] = deriveEncoder[OuProperties].mapJson(_.deepDropNullValues)

  implicit val OuPropertiesDiff = Diff.derived[OuProperties]

}
