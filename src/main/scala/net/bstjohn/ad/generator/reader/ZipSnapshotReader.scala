package net.bstjohn.ad.generator.reader

import cats.effect.{IO, Resource}
import io.circe.parser._
import net.bstjohn.ad.generator.format.computers.Computers
import net.bstjohn.ad.generator.format.containers.Containers
import net.bstjohn.ad.generator.format.domains.Domains
import net.bstjohn.ad.generator.format.gpos.Gpos
import net.bstjohn.ad.generator.format.groups.Groups
import net.bstjohn.ad.generator.format.ous.Ous
import net.bstjohn.ad.generator.format.users.Users
import net.bstjohn.ad.generator.generators.model.EpochSeconds
import net.bstjohn.ad.generator.snapshots.DbSnapshot
import org.apache.commons.io.input.BOMInputStream
import scala.jdk.CollectionConverters._

import java.nio.file.Path
import java.util.zip.{ZipEntry, ZipFile}

object ZipSnapshotReader {

  def read(path: Path): IO[Option[DbSnapshot]] = IO.defer {
    read(path.toString)
  }

  def read(path: String): IO[Option[DbSnapshot]] = IO.defer {
    def fail(message: String) = throw new Exception(s"Failed to read $path - $message")
    val zipFile = new ZipFile(path)
    val entries = zipFile.entries.asScala.toList

    val epoch = entries.flatMap(e => e.getName.substring(0, 14).toLongOption).headOption.getOrElse(fail(""))

    (for {
      computersIO <- entries.find(e => e.getName.endsWith("computers.json")).map(entry =>
        getContents(zipFile, entry).map( contents =>
          decode[Computers](contents).getOrElse(fail("computers.json"))))
      containersIO <- entries.find(e => e.getName.endsWith("containers.json")).map(entry =>
        getContents(zipFile, entry).map( contents =>
          decode[Containers](contents).getOrElse(fail("containers.json"))))
      domainsIO <- entries.find(e => e.getName.endsWith("domains.json")).map(entry =>
        getContents(zipFile, entry).map( contents =>
          decode[Domains](contents).getOrElse(fail("domains.json"))))
      gposIO <- entries.find(e => e.getName.endsWith("gpos.json")).map(entry =>
        getContents(zipFile, entry).map( contents =>
          decode[Gpos](contents).getOrElse(fail("gpos.json"))))
      groupsIO <- entries.find(e => e.getName.endsWith("groups.json")).map(entry =>
        getContents(zipFile, entry).map( contents =>
          decode[Groups](contents).getOrElse(fail("groups.json"))))
      ousIO <- entries.find(e => e.getName.endsWith("ous.json")).map(entry =>
        getContents(zipFile, entry).map( contents =>
          decode[Ous](contents).getOrElse(fail("ous.json"))))
      usersIO <- entries.find(e => e.getName.endsWith("users.json")).map(entry =>
        getContents(zipFile, entry).map( contents =>
          decode[Users](contents).getOrElse(fail("users.json"))))
    } yield {
      for {
        computers <- computersIO
        containers <- containersIO
        domains <- domainsIO
        gpos <- gposIO
        groups <- groupsIO
        ous <- ousIO
        users <- usersIO
      } yield {
        val s = DbSnapshot(
          computers, containers, domains, gpos, groups, ous, users,
          epoch = EpochSeconds(epoch)
        )

        Some(s)
      }
    }).getOrElse(IO.pure(None))
  }

  private def getContents(zipFile: ZipFile, entry: ZipEntry): IO[String] = {
    Resource.make {
      IO(scala.io.Source.fromInputStream(
        new BOMInputStream(zipFile.getInputStream(entry))
      ))
    } { inStream =>
      IO(inStream.close()).handleErrorWith(_ => IO.unit)
    }.use { stream =>
      IO(stream.getLines().mkString)
    }
  }
}
