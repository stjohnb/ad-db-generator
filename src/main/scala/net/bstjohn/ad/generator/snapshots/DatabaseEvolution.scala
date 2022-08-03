package net.bstjohn.ad.generator.snapshots

import cats.effect.IO
import cats.implicits.toTraverseOps
import net.bstjohn.ad.generator.format.common.EntityId.GroupId
import net.bstjohn.ad.generator.format.computers.Computer
import net.bstjohn.ad.generator.format.domains.Domain
import net.bstjohn.ad.generator.format.groups.Group
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

  def domain: Domain = latestSnapshot.domains.toSeq.flatMap(_.data).headOption.getOrElse(???)
  def timestamp: EpochSeconds = latestSnapshot.epoch
  def users: Seq[User] = latestSnapshot.users.toSeq.flatMap(_.data)
  def computers: Seq[Computer] = latestSnapshot.computers.toSeq.flatMap(_.data)
  def groups: Seq[Group] = latestSnapshot.groups.toSeq.flatMap(_.data)

  def group(groupId: GroupId): Group = groups.find(_.ObjectIdentifier == groupId).get
}

object DatabaseEvolution {
  def writeFinalForksToDisk(evolution: EvolutionForks, snapshotsOutputDir: String): IO[Unit] = {
    evolution.finalForks.map { s =>
      DbSnapshot.writeToDisk(s, s"$snapshotsOutputDir/${evolution.scenarioName}/final_forks/")
    }.sequence.map(_ => ())
  }


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
