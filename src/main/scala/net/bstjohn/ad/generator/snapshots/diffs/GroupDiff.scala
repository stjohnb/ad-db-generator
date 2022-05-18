package net.bstjohn.ad.generator.snapshots.diffs

import net.bstjohn.ad.generator.format.groups.Group
import net.bstjohn.ad.generator.format.users.User
import net.bstjohn.ad.generator.snapshots.DbSnapshot

sealed trait GroupDiff

object GroupDiff {

  case class GroupCreated(group: Group) extends GroupDiff
  case class GroupDeleted(group: Group) extends GroupDiff

  case class GroupUpdated(
    initial: Group,
    updated: Group
  ) extends GroupDiff

  def from(s1: DbSnapshot, s2: DbSnapshot): Iterable[GroupDiff] = {
    val createdOrUpdarted = s2.groups.data.flatMap { updated =>
      s1.groups.data.find(g => g.ObjectIdentifier == updated.ObjectIdentifier) match {
        case Some(previous) if previous != updated =>
          Some(GroupUpdated(previous, updated))
        case None =>
          Some(GroupCreated(updated))
        case _ =>
          None
      }
    }

    val deleted = s1.groups.data.flatMap { initial =>
      s2.groups.data.find(g => g.ObjectIdentifier == initial.ObjectIdentifier) match {
        case None =>
          Some(GroupDeleted(initial))
        case _ =>
          None
      }
    }

    createdOrUpdarted ++ deleted
  }
}