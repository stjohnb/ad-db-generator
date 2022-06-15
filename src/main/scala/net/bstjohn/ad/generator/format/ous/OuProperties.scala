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

//  implicit val jsonOptionDiffInstance: Diff[Option[Json]] = JsonDiffInstance.diffForOptionJson(implicitly[Diff[String]])
//  implicit val jsonOptionListDiffInstance: Diff[Option[List[Json]]] = JsonDiffInstance.diffForOptionListJson(implicitly[Diff[String]])

  implicit val OuPropertiesDiff = Diff.derived[OuProperties]

}
