package net.bstjohn.ad.generator.snapshots.diffs

import net.bstjohn.ad.generator.format.users.User
import net.bstjohn.ad.generator.snapshots.DbSnapshot

sealed trait UserDiff

object UserDiff {

  case class UserCreated(user: User) extends UserDiff

  case class UserUpdated(
    initial: User,
    updated: User
  ) extends UserDiff

  def from(s1: DbSnapshot, s2: DbSnapshot): Iterable[UserDiff] = {
    s2.users.data.flatMap { updated =>
      s1.users.data.find(u => u.ObjectIdentifier == updated.ObjectIdentifier) match {
        case Some(previous) if previous != updated =>
          Some(UserUpdated(previous, updated))
        case None =>
          Some(UserCreated(updated))
        case _ =>
          None
      }
    }
  }
}