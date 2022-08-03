package net.bstjohn.ad.generator.reader

import cats.effect.{IO, Resource}
import io.circe.Decoder
import io.circe.parser._
import net.bstjohn.ad.generator.format.common.EntityId.UserId
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

import java.nio.file.Path
import java.util.zip.{ZipEntry, ZipFile}
import scala.jdk.CollectionConverters._
import scala.reflect.ClassTag

object ZipSnapshotReader {

  def read(path: Path, lateralMovementIds: Option[Seq[UserId]]): IO[Option[DbSnapshot]] = IO.defer {
    read(path.toString, lateralMovementIds)
  }

  def read(path: String, lateralMovementIds: Option[Seq[UserId]]): IO[Option[DbSnapshot]] = IO.defer {
    def fail(message: String) = throw new Exception(s"Failed to read $path - $message")
    val zipFile = new ZipFile(path)
    val entries = zipFile.entries.asScala.toList

    val epoch = entries.flatMap(e => e.getName.substring(0, 14).toLongOption).headOption.getOrElse(fail("No epoch"))

    def read[T](fileSuffix: String)(implicit decoder: Decoder[T], tag: ClassTag[T]): IO[Option[T]] = {
      entries.find(e => e.getName.endsWith(fileSuffix)).map(entry =>
        getContents(zipFile, entry).map( contents =>
          Some(decode(contents).fold(e => throw new Exception(s"${e.getMessage} - Failed to read $tag from $contents"), identity))): IO[Option[T]])
    }.getOrElse(IO.pure(None))

    val computersIO = read[Computers]("computers.json")
    val containersIO = read[Containers]("containers.json")
    val domainsIO = read[Domains]("domains.json")
    val gposIO = read[Gpos]("gpos.json")
    val groupsIO = read[Groups]("groups.json")
    val ousIO = read[Ous]("ous.json")
    val usersIO = read[Users]("users.json")

    val lateralMovementIdsIO = entries.find(e => e.getName.endsWith("lateral_movement_ids.json")) match {
      case Some(entry) =>
        getContents(zipFile, entry).map(contents =>
          decode[Option[Seq[UserId]]](contents).fold(e => throw e, identity))
      case None =>
        IO.pure(lateralMovementIds)
    }

    for {
      computers <- computersIO
      containers <- containersIO
      domains <- domainsIO
      gpos <- gposIO
      groups <- groupsIO
      ous <- ousIO
      users <- usersIO
      readLateralMovementIds <- lateralMovementIdsIO
    } yield {
      val s = DbSnapshot(
        computers, containers, domains, gpos, groups, ous, users,
        epoch = EpochSeconds(epoch), readLateralMovementIds
      )

      Some(s)

    }
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
