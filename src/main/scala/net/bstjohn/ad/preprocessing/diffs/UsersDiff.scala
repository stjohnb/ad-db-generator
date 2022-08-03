package net.bstjohn.ad.preprocessing.diffs

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import net.bstjohn.ad.generator.format.ace.Ace
import net.bstjohn.ad.generator.format.users.User
import net.bstjohn.ad.generator.snapshots.DbSnapshot
import net.bstjohn.ad.preprocessing.diffs.UsersDiff.UserUpdated

case class UsersDiff(
  created: Seq[User],
  updated: Seq[UserUpdated],
  deleted: Seq[User]
)

object UsersDiff {

  implicit val UserDiffEncoder: Encoder[UsersDiff] = deriveEncoder[UsersDiff]

  case class UserUpdated(
    name: String,
    acesAdded: Set[Ace],
    acesRemoved: Set[Ace],
  )

  object UserUpdated {
    implicit val UserUpdatedEncoder: Encoder[UserUpdated] = deriveEncoder[UserUpdated]
  }

  val empty = UsersDiff(List.empty, List.empty, List.empty)

  def from(s1: DbSnapshot, s2: DbSnapshot): UsersDiff = {
    val created = s2.users.toSeq.flatMap(_.data).collect {
      case u if !s1.users.toSeq.flatMap(_.data).exists(_.ObjectIdentifier == u.ObjectIdentifier) =>
        u
    }

    val updates = s2.users.toSeq.flatMap(_.data).flatMap { update =>
      s1.users.toSeq.flatMap(_.data).find(g => g.ObjectIdentifier == update.ObjectIdentifier) match {
        case Some(previous) if previous != update =>
          Some(UserUpdated(
            name = previous.Properties.name,
            update.Aces.toSet.diff(previous.Aces.toSet),
            previous.Aces.toSet.diff(update.Aces.toSet)
          ))
        case _ =>
          None
      }
    }

    val deleted = s1.users.toSeq.flatMap(_.data).collect {
      case g if !s2.users.toSeq.flatMap(_.data).exists(_.ObjectIdentifier == g.ObjectIdentifier) =>
        g
    }


    UsersDiff(created, updates, deleted)
  }
}