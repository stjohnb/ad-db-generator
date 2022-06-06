package net.bstjohn.ad.preprocessing

import net.bstjohn.ad.generator.format.users.User
import net.bstjohn.ad.preprocessing.diffs.GroupsDiff

import scala.annotation.tailrec

case class RelatedChanges(
  user: User,
  acesReceived: Seq[AccessControlEntry],
  acesModified: Set[AccessControlEntry]
)

object RelatedChanges {
  def apply(
    user: User,
    groupDiffs: GroupsDiff,
    initialRelations: InvertedRelations,
    finalRelations: InvertedRelations
  ): RelatedChanges = {
    val groupsJoined = groupDiffs.updated
      .filter(_.membersAdded.exists(_.ObjectIdentifier == user.ObjectIdentifier))
      .map(u => u.group.ObjectIdentifier).toSet

    val groupsInherited = groupsJoined.flatMap(g => allGroupsRec(g, finalRelations.groupMemberships, Seq.empty))

    val acesGained = initialRelations.accessControlEntries.filter(ace => groupsInherited.contains(ace.sourceId))

    val initialUserAces = initialRelations.accessControlEntries.filter(ace => ace.sourceId == user.ObjectIdentifier).toSet
    val updatedUserAces = finalRelations.accessControlEntries.filter(ace => ace.sourceId == user.ObjectIdentifier).toSet

    RelatedChanges(
      user = user,
      acesReceived = acesGained,
      acesModified = updatedUserAces -- initialUserAces
    )
  }

  private def allGroupsRec(groupId: String, groupsMap: Map[String, Seq[String]], acc: Seq[String]): Seq[String] = {
    groupsMap.get(groupId) match {
      case None =>
        acc
      case Some(groups) =>
        groups.foldLeft(acc)((acc, groupId) => allGroupsRec(groupId, groupsMap, acc))
    }
  }
}
