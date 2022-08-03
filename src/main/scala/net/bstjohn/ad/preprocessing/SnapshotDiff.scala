package net.bstjohn.ad.preprocessing

import cats.effect.IO
import net.bstjohn.ad.generator.generators.model.EpochSeconds
import net.bstjohn.ad.generator.snapshots.DbSnapshot
import net.bstjohn.ad.preprocessing.diffs._

case class SnapshotDiff (
  from: EpochSeconds,
  to: EpochSeconds,
  changedUsers: UsersDiff,
  changedGroups: GroupsDiff,
  userChanges: Seq[UserChanges],
  groupedUserChanges: Seq[GroupedUserChanges]
)

object SnapshotDiff {

  def from(initial: DbSnapshot, finalSnapshot: DbSnapshot): SnapshotDiff = {
    val initialRelations = InvertedRelations.from(initial)
    val finalRelations = InvertedRelations.from(finalSnapshot)

    val userDiffs = UsersDiff.from(initial, finalSnapshot)
    val groupDiffs = GroupsDiff.from(initial, finalSnapshot)

    val userChanges = finalSnapshot.users.toSeq.flatMap(_.data).map(u =>
      UserChanges(u, groupDiffs, initialRelations, finalRelations, finalSnapshot.lateralMovementIds.getOrElse(Seq.empty))
    ).filter(_.isChanged)

    val groupedUserChanges = GroupedUserChanges.from(
      userChanges = userChanges,
      computers = initial.computers.toSeq.flatMap(_.data) ++ finalSnapshot.computers.toSeq.flatMap(_.data),
      groups = initial.groups.map(_.data).getOrElse(???),
      groupsMap = initialRelations.groupMemberships
    ).filter(_.isChanged)

    SnapshotDiff(initial.epoch, finalSnapshot.epoch, userDiffs, groupDiffs, userChanges, groupedUserChanges)
  }

  def writeUserChanges(diff: SnapshotDiff, path: String): IO[Unit] = {
    val changes = diff.userChanges.filter(_.isChanged)

    println(s"${changes.count(_.isLateralMovement)} lateral movements")

    if(changes.nonEmpty) {
      UserChanges.writeToDisk(changes, s"$path/${diff.from.value}-${diff.to.value}-changes.csv")
    } else {
      IO.pure(())
    }
  }

  def writeAllUserChanges(diffs: Seq[SnapshotDiff], path: String): IO[Unit] = {
    val userChanges = diffs.flatMap(_.userChanges)
    val changedUserChanges = userChanges.filter(_.isChanged)
    val grouped = diffs.flatMap(_.groupedUserChanges)
    val changes = grouped.filter(_.isChanged)
    val lateralMovements = changes.filter(_.isLateralMovement)
    val nonLateralMovements = changes.filterNot(_.isLateralMovement)
    println(
      s"""grouped: ${grouped.size}
         |changes: ${changes.size}
         |lateralMovements: ${lateralMovements.size}
         |nonLateralMovements: ${nonLateralMovements.size}
         |userChanges: ${userChanges.size}
         |changedUserChanges: ${changedUserChanges.size}
         |""".stripMargin)
    val split = nonLateralMovements.map {
      case l if Math.random() < 0.8 => Right(l)
      case l => Left(l)
    }

    val train: Seq[GroupedUserChanges] = split.collect {
      case Right(l) => l
    }
    val test: Seq[GroupedUserChanges] = split.collect {
      case Left(l) => l
    } ++ lateralMovements

    for {
      //    _ <- UserChanges.writeToDisk(diffs.flatMap(_.userChanges), s"$path/all-changes.csv")
      _ <- GroupedUserChanges.writeToDisk(train, s"$path/train.csv")
      _ <- GroupedUserChanges.writeToDisk(test, s"$path/test.csv")
      //    _ <- UserChanges.writeToDisk(diffs.flatMap(_.userChanges).take(20), s"$path/small-changes.csv")
    } yield ()
  }
}
