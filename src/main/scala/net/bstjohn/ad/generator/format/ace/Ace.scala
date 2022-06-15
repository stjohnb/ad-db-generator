package net.bstjohn.ad.generator.format.ace

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.bstjohn.ad.generator.format.common.EntityId.GroupId

case class Ace(
  PrincipalSID: String,
  PrincipalType: AcePrincipalType,
  RightName: RightName,
  IsInherited: Boolean
)


object Ace {
  implicit val AceDecoder: Decoder[Ace] = deriveDecoder[Ace]
  implicit val AceEncoder: Encoder[Ace] = deriveEncoder[Ace]

  def apply(groupId: GroupId, rightName: RightName): Ace = Ace(
    PrincipalSID = groupId.value,
    PrincipalType = AcePrincipalType.Group,
    RightName = rightName,
    IsInherited = false
  )

}