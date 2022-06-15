package net.bstjohn.ad.generator.snapshots

import cats.effect.IO
import cats.implicits.toTraverseOps

case class DatabaseEvolution(
  latestSnapshot: DbSnapshot,
  previousSnapshots: Seq[DbSnapshot]
) {
  def withSnapshot(snapshot: DbSnapshot): DatabaseEvolution = this.copy(
    latestSnapshot = snapshot,
    previousSnapshots = snapshots)

  def snapshots: Seq[DbSnapshot] = this.previousSnapshots :+ this.latestSnapshot
}

object DatabaseEvolution {

  def apply(snapshot: DbSnapshot): DatabaseEvolution = {
    DatabaseEvolution(snapshot, Seq.empty)
  }

  def from(snapshot: DbSnapshot, snapshots: DbSnapshot*): DatabaseEvolution = {
    snapshots.foldLeft(DatabaseEvolution(snapshot))((ev, s) => ev.withSnapshot(s))
  }

  def writeToDisk(db: DatabaseEvolution, path: String): IO[Unit] = {
    db.snapshots.map { s =>
      DbSnapshot.writeToDisk(s, path)
    }.sequence.map(_ => ())
  }
}
