package net.bstjohn.ad.preprocessing

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
  def from(snapshot: DbSnapshot): Seq[AccessControlEntry] = {
    val userAces = snapshot.users.data.flatMap{ user =>
      user.Aces.map { ace =>
        AccessControlEntry(
          AccessControlEntryEntityType(ace.PrincipalType),
          ace.PrincipalSID,
          ace.RightName,
          AccessControlEntryEntityType.User,
          user.ObjectIdentifier
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
          group.ObjectIdentifier
        )
      }
    }

    userAces ++ groupAces
  }
}