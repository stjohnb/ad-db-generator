package net.bstjohn.ad.generator.format.domains

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json, JsonObject}
import net.bstjohn.ad.generator.format.ace.{Ace, AcePrincipalType, RightName}
import net.bstjohn.ad.generator.format.groups.Group

case class Domain(
  ChildObjects: List[DomainChild],
  Trusts: List[Json],
  Links: List[JsonObject],
  Aces: List[Ace],
  ObjectIdentifier: String,
  IsDeleted: Boolean,
  IsACLProtected: Boolean,
  GPOChanges: JsonObject,
  Properties: DomainProperties,
) {
  def withDomainAdminsGroup(group: Group): Domain = {
    copy(
      Aces = Aces ++ List(
        Ace(
          PrincipalSID = group.ObjectIdentifier,
          PrincipalType = AcePrincipalType.Group,
          RightName = RightName.WriteDacl,
          IsInherited = false
        ),
        Ace(
          PrincipalSID = group.ObjectIdentifier,
          PrincipalType = AcePrincipalType.Group,
          RightName = RightName.WriteOwner,
          IsInherited = false
        ),
        Ace(
          PrincipalSID = group.ObjectIdentifier,
          PrincipalType = AcePrincipalType.Group,
          RightName = RightName.AllExtendedRights,
          IsInherited = false
        ),
      )
    )
  }
}

object Domain {
  implicit val DomainDecoder: Decoder[Domain] = deriveDecoder[Domain]
  implicit val DomainEncoder: Encoder[Domain] = deriveEncoder[Domain]

}