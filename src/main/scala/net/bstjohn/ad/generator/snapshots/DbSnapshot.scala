package net.bstjohn.ad.generator.snapshots

import cats.effect.IO
import net.bstjohn.ad.generator.format.domains.{Domain, Domains}
import net.bstjohn.ad.generator.format.groups.{Group, Groups}
import net.bstjohn.ad.generator.format.users.{User, Users}
import net.bstjohn.ad.generator.generators.model.EpochSeconds
import io.circe.syntax._
import net.bstjohn.ad.generator.format.computers.{Computer, Computers}

import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.io.File

case class DbSnapshot(
  domains: Domains,
  users: Users,
  groups: Groups,
  computers: Computers,
  epoch: EpochSeconds
)

object DbSnapshot {
  def apply(
    domain: Domain,
    users: Iterable[User],
    groups: Iterable[Group],
    computers: Iterable[Computer],
    epoch: EpochSeconds
  ): DbSnapshot = {
    DbSnapshot(
      Domains(List(domain)),
      Users(users),
      Groups(groups),
      Computers(computers),
      epoch
    )
  }

  def writeToDisk(snapshot: DbSnapshot, directory: String): IO[Unit] = IO.delay {
    new File(directory).mkdirs()
    val f = new File(s"$directory/${snapshot.epoch.toDateString}_BloodHound.zip")

    val out = new ZipOutputStream(new FileOutputStream(f))

    out.putNextEntry(new ZipEntry(s"${snapshot.epoch.toDateString}_computers.json"))
    out.write(snapshot.computers.asJson.spaces2.getBytes)
    out.closeEntry()

    out.putNextEntry(new ZipEntry(s"${snapshot.epoch.toDateString}_domains.json"))
    out.write(snapshot.domains.asJson.spaces2.getBytes)
    out.closeEntry()

    out.putNextEntry(new ZipEntry(s"${snapshot.epoch.toDateString}_users.json"))
    out.write(snapshot.users.asJson.spaces2.getBytes)
    out.closeEntry()

    out.putNextEntry(new ZipEntry(s"${snapshot.epoch.toDateString}_groups.json"))
    out.write(snapshot.groups.asJson.spaces2.getBytes)
    out.closeEntry()

    out.close()
  }
}
