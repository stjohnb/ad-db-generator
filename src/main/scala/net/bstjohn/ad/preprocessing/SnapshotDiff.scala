package net.bstjohn.ad.preprocessing

import cats.effect.IO
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax._
import net.bstjohn.ad.generator.snapshots.DbSnapshot
import net.bstjohn.ad.preprocessing.diffs._
import java.io.{File, PrintWriter}

case class SnapshotDiff (
  changedUsers: UsersDiff,
  changedGroups: GroupsDiff,
  userChanges: Seq[UserChanges]
)

object SnapshotDiff {

  implicit val SnapshotDiffEncoder: Encoder[SnapshotDiff] = deriveEncoder[SnapshotDiff]

  def from(initial: DbSnapshot, finalSnapshot: DbSnapshot): SnapshotDiff = {
    val initialRelations = InvertedRelations.from(initial)
    val finalRelations = InvertedRelations.from(finalSnapshot)

    val userDiffs = UsersDiff.from(initial, finalSnapshot)
    val groupDiffs = GroupsDiff.from(initial, finalSnapshot)

    val userChanges = finalSnapshot.users.data.map(u =>
      UserChanges(u, groupDiffs, initialRelations, finalRelations, finalSnapshot.lateralMovementIds.contains(u.ObjectIdentifier))
    )

    SnapshotDiff(userDiffs, groupDiffs, userChanges)
  }

  def write(diff: SnapshotDiff, path: String): IO[Unit] = {
    writeToFile(diff.asJson.spaces2, path)
  }

  private def writeToFile(contents: String, path: String): IO[Unit] = IO.delay {
    val pw = new PrintWriter(new File(path))
    try pw.write(contents) finally pw.close()
    println(s"Diff written to $path")
  }
}
