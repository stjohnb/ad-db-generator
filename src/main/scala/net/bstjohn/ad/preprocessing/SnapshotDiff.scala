package net.bstjohn.ad.preprocessing

import cats.effect.IO
import net.bstjohn.ad.generator.format.common.EntityId.UserId
import net.bstjohn.ad.generator.generators.model.EpochSeconds
import net.bstjohn.ad.generator.snapshots.DbSnapshot
import net.bstjohn.ad.preprocessing.diffs._

case class SnapshotDiff (
  from: EpochSeconds,
  to: EpochSeconds,
  changedUsers: UsersDiff,
  changedGroups: GroupsDiff,
  userChanges: Seq[UserChanges]
)

object SnapshotDiff {

  def from(initial: DbSnapshot, finalSnapshot: DbSnapshot): SnapshotDiff = {
    val initialRelations = InvertedRelations.from(initial)
    val finalRelations = InvertedRelations.from(finalSnapshot)

    val userDiffs = UsersDiff.from(initial, finalSnapshot)
    val groupDiffs = GroupsDiff.from(initial, finalSnapshot)

    val userChanges = finalSnapshot.users.data.map(u =>
      UserChanges(u, groupDiffs, initialRelations, finalRelations, finalSnapshot.lateralMovementIds)
    ).filter(_.isChanged)

    SnapshotDiff(initial.epoch, finalSnapshot.epoch, userDiffs, groupDiffs, userChanges)
  }

  def writeUserChanges(diff: SnapshotDiff, path: String): IO[Unit] = {
    UserChanges.writeToDisk(diff.userChanges, s"$path/${diff.from.value}-${diff.to.value}-changes.csv")
  }

  def writeAllUserChanges(diffs: Seq[SnapshotDiff], path: String): IO[Unit] = {
    val userChanges = diffs.flatMap(_.userChanges)
    val lateralMovements = userChanges.filter(_.isLateralMovement)
    val nonLateralMovements = userChanges.filterNot(_.isLateralMovement)
    val split = nonLateralMovements.map {
      case l if Math.random() < 0.7 => Right(l)
      case l => Left(l)
    }

    val train: Seq[UserChanges] = split.collect {
      case Right(l) => l
    }
    val test: Seq[UserChanges] = split.collect {
      case Left(l) => l
    } ++ lateralMovements

    for {
      //    _ <- UserChanges.writeToDisk(diffs.flatMap(_.userChanges), s"$path/all-changes.csv")
      _ <- UserChanges.writeToDisk(train, s"$path/train.csv")
      _ <- UserChanges.writeToDisk(test, s"$path/test.csv")
      //    _ <- UserChanges.writeToDisk(diffs.flatMap(_.userChanges).take(20), s"$path/small-changes.csv")
    } yield ()
  }
}
