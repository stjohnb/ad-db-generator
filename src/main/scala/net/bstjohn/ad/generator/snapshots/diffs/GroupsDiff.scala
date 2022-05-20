package net.bstjohn.ad.generator.snapshots.diffs

import com.softwaremill.diffx.DiffResult
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import net.bstjohn.ad.generator.format.ace.Ace
import net.bstjohn.ad.generator.format.groups.{Group, GroupMember}
import net.bstjohn.ad.generator.snapshots.DbSnapshot
import net.bstjohn.ad.generator.snapshots.diffs.GroupsDiff.GroupUpdated

case class GroupsDiff(
  created: List[Group],
  updated: List[GroupUpdated],
  deleted: List[Group],
)

object GroupsDiff {

  implicit val GroupDiffEncoder: Encoder[GroupsDiff] = deriveEncoder[GroupsDiff]

  case class GroupUpdated(
    name: String,
    acesAdded: Set[Ace],
    membersAdded: Set[GroupMember]
  )

  object GroupUpdated {
//    import DiffResultJsonFormat._

    implicit val GroupUpdatedEncoder: Encoder[GroupUpdated] = deriveEncoder[GroupUpdated]

//    def apply(previous: Group, current: Group): GroupUpdated = {
//      import com.softwaremill.diffx.generic.auto._
//      import com.softwaremill.diffx._
//
//      GroupUpdated(previous, current, compare(previous, current))
//    }
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
            name = if(previous.Properties.name == update.Properties.name) previous.Properties.name
              else s"Changed: '${previous.Properties.name}' to '${update.Properties.name}' ",
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