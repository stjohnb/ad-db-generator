package net.bstjohn.ad.preprocessing.diffs

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import net.bstjohn.ad.generator.format.ace.Ace
import net.bstjohn.ad.generator.format.groups.{Group, GroupMember}
import net.bstjohn.ad.generator.snapshots.DbSnapshot
import net.bstjohn.ad.preprocessing.diffs.GroupsDiff.GroupUpdated

case class GroupsDiff(
  created: Seq[GroupUpdated],
  updated: Seq[GroupUpdated],
  deleted: Seq[Group],
) {
  def all: Seq[GroupUpdated] = created ++ updated
}

object GroupsDiff {

  implicit val GroupDiffEncoder: Encoder[GroupsDiff] = deriveEncoder[GroupsDiff]

  case class GroupUpdated(
    group: Group,
    acesAdded: Set[Ace],
    membersAdded: Set[GroupMember]
  )

  object GroupUpdated {
    implicit val GroupUpdatedEncoder: Encoder[GroupUpdated] = deriveEncoder[GroupUpdated]
  }

  def from(
    initial: DbSnapshot,
    updated: DbSnapshot
  ): GroupsDiff = {
    val created = updated.groups.toSeq.flatMap(_.data).collect {
      case g if !initial.groups.toSeq.flatMap(_.data).exists(_.ObjectIdentifier == g.ObjectIdentifier) =>
        GroupUpdated(g, acesAdded = g.Aces.toSet, membersAdded = g.Members.toSet)
    }

    val updates = updated.groups.toSeq.flatMap(_.data).flatMap { update =>
      initial.groups.toSeq.flatMap(_.data).find(g => g.ObjectIdentifier == update.ObjectIdentifier) flatMap { previous =>
        val acesAdded = update.Aces.toSet -- previous.Aces.toSet
        val membersAdded = update.Members.toSet -- previous.Members.toSet

        if (acesAdded.nonEmpty || membersAdded.nonEmpty) {
          Some(GroupUpdated(update, acesAdded, membersAdded))
        } else {
          None
        }
      }
    }

    val deleted = initial.groups.toSeq.flatMap(_.data).collect {
      case g if !updated.groups.toSeq.flatMap(_.data).exists(_.ObjectIdentifier == g.ObjectIdentifier) =>
        g
    }


    GroupsDiff(created, updates, deleted)
  }
}