package net.bstjohn.ad.generator.snapshots

import cats.effect.IO
import cats.implicits.toTraverseOps
import net.bstjohn.ad.generator.format.domains.Domain
import net.bstjohn.ad.generator.format.users.User
import net.bstjohn.ad.generator.generators.model.EpochSeconds

case class DatabaseEvolution(
  scenarioName: String,
  latestSnapshot: DbSnapshot,
  previousSnapshots: Seq[DbSnapshot]
) {
  def snapshots: Seq[DbSnapshot] = this.previousSnapshots :+ this.latestSnapshot

  def withSnapshot(snapshot: DbSnapshot): DatabaseEvolution = {
    if(snapshots.exists(_.epoch == snapshot.epoch)) {
      throw new Exception("Duplicate epoch")
    }
    this.copy(
      latestSnapshot = snapshot,
      previousSnapshots = snapshots)
  }

  def updated(update: DbSnapshot => DbSnapshot): DatabaseEvolution = withSnapshot(update(latestSnapshot))

  def first: Option[DbSnapshot] = snapshots.headOption
  def second: Option[DbSnapshot] = snapshots.tail.headOption

  def domain: Domain = latestSnapshot.domains.data.headOption.getOrElse(???)
  def timestamp: EpochSeconds = latestSnapshot.epoch
  def users: Seq[User] = latestSnapshot.users.data
}

object DatabaseEvolution {

  def apply(name: String, snapshot: DbSnapshot): DatabaseEvolution = {
    DatabaseEvolution(name, snapshot, Seq.empty)
  }

  def from(name: String, snapshot: DbSnapshot, snapshots: DbSnapshot*): DatabaseEvolution = {
    snapshots.foldLeft(DatabaseEvolution(name, snapshot))((ev, s) => ev.withSnapshot(s))
  }

  def writeToDisk(db: DatabaseEvolution, snapshotsOutputDir: String): IO[Unit] = {
    db.snapshots.map { s =>
      DbSnapshot.writeToDisk(s, s"$snapshotsOutputDir/${db.scenarioName}")
    }.sequence.map(_ => ())
  }
}
