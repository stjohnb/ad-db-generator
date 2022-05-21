package net.bstjohn.ad.generator.reader

import cats.effect.{IO, Resource}
import io.circe.Decoder
import io.circe.syntax._
import io.circe.parser._
import net.bstjohn.ad.generator.format.domains.Domains
import net.bstjohn.ad.generator.format.groups.Groups
import net.bstjohn.ad.generator.format.users.Users
import net.bstjohn.ad.generator.generators.model.EpochSeconds
import net.bstjohn.ad.generator.snapshots
import net.bstjohn.ad.generator.snapshots.DbSnapshot
import org.apache.commons.io.input.BOMInputStream

import java.util.zip.{ZipEntry, ZipFile}
import scala.util.Try

object ZipSnapshotReader {
  def read(path: String): IO[Option[DbSnapshot]] = IO.defer {
    val zipFile = new ZipFile(path)

    import scala.jdk.CollectionConverters._

    val entries = zipFile.entries.asScala

    val users = entries.find(e => e.getName.endsWith("users.json")).map(entry =>
      getContents(zipFile, entry).map( contents =>
        decode[Users](contents).getOrElse(???)
      )
    )

    val groups = entries.find(e => e.getName.endsWith("groups.json")).map(entry =>
      getContents(zipFile, entry).map( contents =>
        decode[Groups](contents).getOrElse(???)
      )
    )

    val domains = entries.find(e => e.getName.endsWith("domains.json")).map(entry =>
      getContents(zipFile, entry).map( contents =>
        decode[Domains](contents).getOrElse(???)
      )
    )

    val epoch = entries.flatMap(e => e.getName.substring(0, 14).toLongOption).toList.headOption.getOrElse(???)

    (for {
      u <- users
      g <- groups
      d <- domains
    } yield {
      for {
        uu <- u
        gg <- g
        dd <- d
      } yield {
        val s = snapshots.DbSnapshot(
          domains = dd,
          users = uu,
          groups = gg,
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
      IO(inStream.close()).handleErrorWith(_ => IO.unit) // release
    }.use { stream =>
      IO(stream.getLines().mkString)
    }
  }
}
