package net.bstjohn.ad.generator.snapshots

import cats.effect.IO
import io.circe.syntax._
import net.bstjohn.ad.generator.format.common.EntityId.UserId
import net.bstjohn.ad.generator.format.computers.{Computer, Computers}
import net.bstjohn.ad.generator.format.containers.Containers
import net.bstjohn.ad.generator.format.domains.{Domain, Domains}
import net.bstjohn.ad.generator.format.gpos.Gpos
import net.bstjohn.ad.generator.format.groups.{Group, Groups}
import net.bstjohn.ad.generator.format.ous.Ous
import net.bstjohn.ad.generator.format.users.{User, Users}
import net.bstjohn.ad.generator.generators.model.EpochSeconds

import java.io.{File, FileOutputStream}
import java.util.zip.{ZipEntry, ZipOutputStream}
import scala.util.Random

case class DbSnapshot(
  computers: Option[Computers],
  containers: Option[Containers],
  domains: Option[Domains],
  gpos: Option[Gpos],
  groups: Option[Groups],
  ous: Option[Ous],
  users: Option[Users],
  epoch: EpochSeconds,
  lateralMovementIds: Option[Seq[UserId]]
) {
  def withUpdatedGroup(group: Group): DbSnapshot = {
    val defined = groups.getOrElse(Groups.Empty)
    copy(groups = Some(defined.copy(
      data = defined.data.toList.filter(g => g.ObjectIdentifier != group.ObjectIdentifier) :+ group)))
  }

  def withUpdatedGroups(groups: Seq[Group]): DbSnapshot =
    groups.foldLeft(this)((s, g) => s.withUpdatedGroup(g))

  def withUpdatedUser(user: User): DbSnapshot = {
    val defined = users.getOrElse(Users.Empty)
    copy(users = Some(defined.copy(
        data = defined.data.toList.filter(d => d.ObjectIdentifier != user.ObjectIdentifier) :+ user)))
  }

  def withUpdatedUsers(users: Seq[User]): DbSnapshot =
    users.foldLeft(this)((s, u) => s.withUpdatedUser(u))

  def withNewComputer(computer: Computer): DbSnapshot = {
    val defined = computers.getOrElse(Computers.Empty)
    copy(computers = Some(defined.copy(data = defined.data :+ computer)))
  }

  def withNewComputers(newComputers: Seq[Computer]): DbSnapshot = {
    val defined = computers.getOrElse(Computers.Empty)
    copy(computers = Some(defined.copy(data = defined.data ++ newComputers)))
  }

  def randomSessions(): DbSnapshot = (for {
    definedUsers <- users
    definedComputers <- computers
  } yield {
    val loggedInUsers = Random.shuffle(definedUsers.data).take(10)

    copy(
      computers = Some(definedComputers.updated { computer =>
        computer.withSessions(loggedInUsers)
      })
    ).timestamp(epoch.plusMinutes(1))
  }).getOrElse(???)

  def timestamp(epoch: EpochSeconds): DbSnapshot =
    this.copy(epoch = epoch)

  def withLateralMovementIds(lateralMovementIds: Seq[UserId]): DbSnapshot =
    this.copy(lateralMovementIds = Some(lateralMovementIds))

  def clearLateralMovementIds: DbSnapshot =
    this.copy(lateralMovementIds = None).timestamp(epoch.plusMinutes(1))

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
      Some(Computers(computers)),
      Some(Containers(List.empty)),
      Some(Domains(List(domain))),
      Some(Gpos(List.empty)),
      Some(Groups(groups)),
      Some(Ous(List.empty)),
      Some(Users(users)),
      epoch,
      Some(lateralMovementIds)
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

//    out.putNextEntry(new ZipEntry(s"${dateString}_lateral_movement_ids.json"))
//    out.write(snapshot.lateralMovementIds.asJson.spaces2.getBytes)
//    out.closeEntry()

    out.close()
  }
}
