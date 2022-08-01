package net.bstjohn.ad.generator.format.computers

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.bstjohn.ad.generator.format.groups.Group

case class LocalAdmin(
  ObjectIdentifier: String,
  ObjectType: LocalAdminType
)


object LocalAdmin {

  def fromGroup(group: Group): LocalAdmin = {
    LocalAdmin(
      group.ObjectIdentifier.value,
      LocalAdminType.Group
    )
  }

  implicit val LocalAdminDecoder: Decoder[LocalAdmin] = deriveDecoder[LocalAdmin]
  implicit val LocalAdminEncoder: Encoder[LocalAdmin] = deriveEncoder[LocalAdmin]

}
