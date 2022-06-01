package net.bstjohn.ad.generator.snapshots

import cats.effect.IO
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax._
import net.bstjohn.ad.generator.snapshots.diffs.{GroupsDiff, UsersDiff}

case class SnapshotDiff (
  changedUsers: UsersDiff,
  changedGroups: GroupsDiff
)

object SnapshotDiff {

  implicit val SnapshotDiffEncoder: Encoder[SnapshotDiff] = deriveEncoder[SnapshotDiff]

  def from(initial: DbSnapshot, update: DbSnapshot): SnapshotDiff = {
    val userDiffs = UsersDiff.from(initial, update)
    val groupDiffs = GroupsDiff.from(initial, update)

    SnapshotDiff(
      userDiffs,
      groupDiffs
    )
  }

  def write(diff: SnapshotDiff, path: String): IO[Unit] = {
    writeToFile(diff.asJson.spaces2, path)
  }

  import java.io.File
  import java.io.PrintWriter

  private def writeToFile(contents: String, path: String): IO[Unit] = IO.delay {
    val pw = new PrintWriter(new File(path))
    try pw.write(contents) finally pw.close()
    println(s"Diff written to $path")
  }
}
