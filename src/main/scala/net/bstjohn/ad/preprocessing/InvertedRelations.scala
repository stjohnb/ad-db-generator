package net.bstjohn.ad.preprocessing

import net.bstjohn.ad.generator.format.groups.{Group, GroupMember, GroupMemberType}
import net.bstjohn.ad.generator.snapshots.DbSnapshot

case class InvertedRelations(
  groupMemberships: Map[String, Seq[String]],
  accessControlEntries: Seq[AccessControlEntry]
)

object InvertedRelations {
  def from(snapshot: DbSnapshot): InvertedRelations = {
    val groupMemberships = snapshot.groups.data.map { group =>
      val memberGroups = group.Members.collect {
        case GroupMember(groupId, GroupMemberType.Group) =>
          groupId
      }

      group.ObjectIdentifier -> memberGroups
    }.toMap

    val accessControlEntries = AccessControlEntry.from(snapshot)

    InvertedRelations(groupMemberships, accessControlEntries)
  }
}
