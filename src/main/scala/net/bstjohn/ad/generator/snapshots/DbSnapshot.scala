package net.bstjohn.ad.generator.snapshots

import cats.effect.IO
import net.bstjohn.ad.generator.format.domains.{Domain, Domains}
import net.bstjohn.ad.generator.format.groups.{Group, Groups}
import net.bstjohn.ad.generator.format.users.{User, Users}
import net.bstjohn.ad.generator.generators.model.EpochSeconds
import io.circe.syntax._
import net.bstjohn.ad.generator.format.common.EntityId.UserId
import net.bstjohn.ad.generator.format.computers.{Computer, Computers}
import net.bstjohn.ad.generator.format.containers.Containers
import net.bstjohn.ad.generator.format.gpos.Gpos
import net.bstjohn.ad.generator.format.ous.Ous

import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.io.File

case class DbSnapshot(
  computers: Computers,
  containers: Containers,
  domains: Domains,
  gpos: Gpos,
  groups: Groups,
  ous: Ous,
  users: Users,
  epoch: EpochSeconds,
  lateralMovementIds: Seq[UserId]
) {
  def withUpdatedGroup(group: Group): DbSnapshot =
    copy(groups = groups.copy(
        data = groups.data.toList.filter(g => g.ObjectIdentifier != group.ObjectIdentifier) :+ group))

  def withUpdatedGroups(groups: Seq[Group]): DbSnapshot =
    groups.foldLeft(this)((s, g) => s.withUpdatedGroup(g))

  def withUpdatedUser(user: User): DbSnapshot =
    copy(users = users.copy(
        data = users.data.toList.filter(d => d.ObjectIdentifier != user.ObjectIdentifier) :+ user))

  def withUpdatedUsers(users: Seq[User]): DbSnapshot =
    users.foldLeft(this)((s, u) => s.withUpdatedUser(u))

  def withNewComputer(computer: Computer): DbSnapshot =
    this.copy(computers = this.computers.copy(data = this.computers.data :+ computer))

  def withNewComputers(computers: Seq[Computer]): DbSnapshot =
    this.copy(computers = this.computers.copy(data = this.computers.data ++ computers))

  def timestamp(epoch: EpochSeconds): DbSnapshot =
    this.copy(epoch = epoch)

  def withLateralMovementIds(lateralMovementIds: Seq[UserId]): DbSnapshot =
    this.copy(lateralMovementIds = lateralMovementIds)

  def clearLateralMovementIds: DbSnapshot =
    this.copy(lateralMovementIds = Seq.empty).timestamp(epoch.plusMinutes(1))

}

object DbSnapshot {
  def apply(
    domain: Domain,
    users: Seq[User],
    groups: Seq[Group],
    computers: Seq[Computer],
    epoch: EpochSeconds,
    lateralMovementIds: Seq[UserId]
  ): DbSnapshot = {
    DbSnapshot(
      Computers(computers),
      Containers(List.empty),
      Domains(List(domain)),
      Gpos(List.empty),
      Groups(groups),
      Ous(List.empty),
      Users(users),
      epoch,
      lateralMovementIds
    )
  }

  def writeToDisk(snapshot: DbSnapshot, directory: String): IO[Unit] = IO.delay {
    new File(directory).mkdirs()
    val destination = s"$directory/${snapshot.epoch.toDateString}_BloodHound.zip"
    val f = new File(destination)

    if(f.exists()) {
      throw new Exception(s"$f already exists")
    }

    val out = new ZipOutputStream(new FileOutputStream(f))

    val dateString = snapshot.epoch.toDateString

    out.putNextEntry(new ZipEntry(s"${dateString}_containers.json"))
    out.write(snapshot.containers.asJson.spaces2.getBytes)
    out.closeEntry()

    out.putNextEntry(new ZipEntry(s"${dateString}_computers.json"))
    out.write(snapshot.computers.asJson.spaces2.getBytes)
    out.closeEntry()

    out.putNextEntry(new ZipEntry(s"${dateString}_domains.json"))
    out.write(snapshot.domains.asJson.spaces2.getBytes)
    out.closeEntry()

    out.putNextEntry(new ZipEntry(s"${dateString}_gpos.json"))
    out.write(snapshot.gpos.asJson.spaces2.getBytes)
    out.closeEntry()

    out.putNextEntry(new ZipEntry(s"${dateString}_groups.json"))
    out.write(snapshot.groups.asJson.spaces2.getBytes)
    out.closeEntry()

    out.putNextEntry(new ZipEntry(s"${dateString}_ous.json"))
    out.write(snapshot.ous.asJson.spaces2.getBytes)
    out.closeEntry()

    out.putNextEntry(new ZipEntry(s"${dateString}_users.json"))
    out.write(snapshot.users.asJson.spaces2.getBytes)
    out.closeEntry()

    out.putNextEntry(new ZipEntry(s"${dateString}_lateral_movement_ids.json"))
    out.write(snapshot.lateralMovementIds.asJson.spaces2.getBytes)
    out.closeEntry()

    out.close()
  }
}
