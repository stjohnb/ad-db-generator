package net.bstjohn.ad.preprocessing

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import net.bstjohn.ad.generator.format.ace.{AcePrincipalType, RightName}
import net.bstjohn.ad.generator.snapshots.DbSnapshot

case class AccessControlEntry(
  sourceType: AccessControlEntryEntityType,
  sourceId: String,
  rightName: RightName,
  targetType: AccessControlEntryEntityType,
  targetId: String,
)


object AccessControlEntry {

  implicit val AccessControlEntryDecoder: Decoder[AccessControlEntry] = deriveDecoder[AccessControlEntry]
  implicit val AccessControlEntryEncoder: Encoder[AccessControlEntry] = deriveEncoder[AccessControlEntry]

  def from(snapshot: DbSnapshot): Seq[AccessControlEntry] = {
    val userAces = snapshot.users.data.flatMap{ user =>
      user.Aces.map { ace =>
        AccessControlEntry(
          AccessControlEntryEntityType(ace.PrincipalType),
          ace.PrincipalSID,
          ace.RightName,
          AccessControlEntryEntityType.User,
          user.ObjectIdentifier.value
        )
      }
    }

    val groupAces = snapshot.groups.data.flatMap{ group =>
      group.Aces.map { ace =>
        AccessControlEntry(
          AccessControlEntryEntityType(ace.PrincipalType),
          ace.PrincipalSID,
          ace.RightName,
          AccessControlEntryEntityType.Group,
          group.ObjectIdentifier.value
        )
      }
    }

    userAces ++ groupAces
  }
}