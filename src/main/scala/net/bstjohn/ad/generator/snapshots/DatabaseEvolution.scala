package net.bstjohn.ad.generator.snapshots

import cats.effect.IO
import cats.implicits.toTraverseOps

case class DatabaseEvolution(
  snapshots: Iterable[DbSnapshot]
)

object DatabaseEvolution {
  def apply(snapshots: DbSnapshot*): DatabaseEvolution = {
    DatabaseEvolution(snapshots)
  }

  def writeToDisk(db: DatabaseEvolution, path: String): IO[Unit] = {
    db.snapshots.map(s =>
      DbSnapshot.writeToDisk(s, path)
    ).toList.sequence.map(_ => ())
  }
}
