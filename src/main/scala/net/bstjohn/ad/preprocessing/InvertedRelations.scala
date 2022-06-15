package net.bstjohn.ad.preprocessing

import net.bstjohn.ad.generator.format.common.EntityId.GroupId
import net.bstjohn.ad.generator.format.groups.{Group, GroupMember, GroupMemberType}
import net.bstjohn.ad.generator.snapshots.DbSnapshot

case class InvertedRelations(
  groupMemberships: Map[GroupId, Seq[GroupId]],
  accessControlEntries: Seq[AccessControlEntry]
)

object InvertedRelations {
  def from(snapshot: DbSnapshot): InvertedRelations = {
    val groupMemberships = snapshot.groups.data.map { group =>
      val memberGroups = group.Members.collect {
        case GroupMember(id, GroupMemberType.Group) =>
          GroupId(id)
      }

      group.ObjectIdentifier -> memberGroups
    }.toMap

    val accessControlEntries = AccessControlEntry.from(snapshot)

    InvertedRelations(groupMemberships, accessControlEntries)
  }
}
