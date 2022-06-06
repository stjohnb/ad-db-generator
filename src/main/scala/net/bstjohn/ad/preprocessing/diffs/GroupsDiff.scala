package net.bstjohn.ad.preprocessing.diffs

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import net.bstjohn.ad.generator.format.ace.Ace
import net.bstjohn.ad.generator.format.groups.{Group, GroupMember}
import net.bstjohn.ad.generator.snapshots.DbSnapshot
import net.bstjohn.ad.preprocessing.diffs.GroupsDiff.GroupUpdated

case class GroupsDiff(
  created: Seq[Group],
  updated: Seq[GroupUpdated],
  deleted: Seq[Group],
)

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
    val created = updated.groups.data.collect {
      case g if !initial.groups.data.exists(_.ObjectIdentifier == g.ObjectIdentifier) =>
        g
    }

    val updates = updated.groups.data.flatMap { update =>
      initial.groups.data.find(g => g.ObjectIdentifier == update.ObjectIdentifier) match {
        case Some(previous) if previous.Aces.size < update.Aces.size || previous.Members.size < update.Members.size  =>
          Some(GroupUpdated(
            update,
            acesAdded = update.Aces.toSet -- previous.Aces.toSet,
            membersAdded = update.Members.toSet -- previous.Members.toSet
          ))
        case _ =>
          None
      }
    }

    val deleted = initial.groups.data.collect {
      case g if !updated.groups.data.exists(_.ObjectIdentifier == g.ObjectIdentifier) =>
        g
    }


    GroupsDiff(created, updates, deleted)
  }
}