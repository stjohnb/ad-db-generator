package net.bstjohn.ad.generator.snapshots.diffs

import com.softwaremill.diffx.DiffResult
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import net.bstjohn.ad.generator.format.ace.Ace
import net.bstjohn.ad.generator.format.users.User
import net.bstjohn.ad.generator.snapshots.DbSnapshot
import net.bstjohn.ad.generator.snapshots.diffs.UsersDiff.UserUpdated

case class UsersDiff(
  created: Iterable[User],
  updated: Iterable[UserUpdated],
  deleted: Iterable[User]
)

object UsersDiff {

  implicit val UserDiffEncoder: Encoder[UsersDiff] = deriveEncoder[UsersDiff]

  case class UserUpdated(
//    previous: User,
//    current: User,
    name: String,
    acesAdded: Set[Ace],
    acesRemoved: Set[Ace],
//    diffResult: DiffResult
  )

  object UserUpdated {
//    import DiffResultJsonFormat._

    implicit val UserUpdatedEncoder: Encoder[UserUpdated] = deriveEncoder[UserUpdated]

//    def apply(previous: User, current: User): UserUpdated = {
//      import com.softwaremill.diffx.generic.auto._
//      import com.softwaremill.diffx._
//
//      UserUpdated(previous, current, compare(previous, current))
//    }

  }

  val empty = UsersDiff(List.empty, List.empty, List.empty)

  def from(s1: DbSnapshot, s2: DbSnapshot): UsersDiff = {
    val created = s2.users.data.collect {
      case u if !s1.users.data.exists(_.ObjectIdentifier == u.ObjectIdentifier) =>
        u
    }

    val updates = s2.users.data.flatMap { update =>
      s1.users.data.find(g => g.ObjectIdentifier == update.ObjectIdentifier) match {
        case Some(previous) if previous != update =>
          Some(UserUpdated(
//            previous,
//            update,
            name = previous.Properties.name,
            update.Aces.toSet.diff(previous.Aces.toSet),
            previous.Aces.toSet.diff(update.Aces.toSet)
          ))
        case _ =>
          None
      }
    }

    val deleted = s1.users.data.collect {
      case g if !s2.users.data.exists(_.ObjectIdentifier == g.ObjectIdentifier) =>
        g
    }


    UsersDiff(created, updates, deleted)
  }
}