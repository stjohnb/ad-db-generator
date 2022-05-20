package net.bstjohn.ad.generator.snapshots.diffs

import com.softwaremill.diffx.DiffResult
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import net.bstjohn.ad.generator.format.groups.Group
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
    previous: Group,
    current: Group,
    diff: DiffResult
  )

  object GroupUpdated {
    import DiffResultJsonFormat._

    implicit val GroupUpdatedEncoder: Encoder[GroupUpdated] = deriveEncoder[GroupUpdated]

    def apply(previous: Group, current: Group): GroupUpdated = {
      import com.softwaremill.diffx.generic.auto._
      import com.softwaremill.diffx._

      GroupUpdated(previous, current, compare(previous, current))
    }
  }

  def from(s1: DbSnapshot, s2: DbSnapshot): GroupsDiff = {
    val created = s2.groups.data.collect {
      case g if !s1.groups.data.exists(_.ObjectIdentifier == g.ObjectIdentifier) =>
        g
    }

    val updates = s2.groups.data.flatMap { update =>
      s1.groups.data.find(g => g.ObjectIdentifier == update.ObjectIdentifier) match {
        case Some(previous) if previous != update =>
          Some(GroupUpdated(previous, update))
        case _ =>
          None
      }
    }

    val deleted = s1.groups.data.collect {
      case g if !s2.groups.data.exists(_.ObjectIdentifier == g.ObjectIdentifier) =>
        g
    }


    GroupsDiff(created, updates, deleted)
  }
}