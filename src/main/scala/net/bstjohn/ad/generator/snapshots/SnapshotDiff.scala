package net.bstjohn.ad.generator.snapshots

import net.bstjohn.ad.generator.snapshots.diffs.UserDiff

case class SnapshotDiff (
  changedUsers: Iterable[UserDiff]
)

object SnapshotDiff {
  def from(s1: DbSnapshot, s2: DbSnapshot): SnapshotDiff = {
    val userDiffs = UserDiff.from(s1, s2)

    SnapshotDiff(
      userDiffs
    )
  }
}
