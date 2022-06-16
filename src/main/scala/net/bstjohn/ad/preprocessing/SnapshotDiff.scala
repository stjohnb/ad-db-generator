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
    UserChanges.writeToDisk(diffs.flatMap(_.userChanges), s"$path/all-changes.csv")
  }
}
