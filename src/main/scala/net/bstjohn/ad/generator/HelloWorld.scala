package net.bstjohn.ad.generator

import cats.effect.IO
import cats.implicits.{catsSyntaxApplicativeId, toTraverseOps}
import net.bstjohn.ad.generator.generators.DbGenerator
import net.bstjohn.ad.generator.reader.ZipSnapshotReader
import net.bstjohn.ad.generator.snapshots.{DatabaseEvolution, SnapshotDiff}
import org.apache.commons.io.FileUtils

import java.io.File

object HelloWorld {

  val Root = "/Users/brendanstjohn/queens/ad-db-snapshots/"

  val timestamps = List(
    "20220506180850",
    "20220506183026",
    "20220507084916",
    "20220507091000",
    "20220507092238",
    "20220509120734",
    "20220509121121",
    "20220509121320",
//    "20220509121936",
    "20220509125013",
    "20220509135532",
    "20220520162651",
  )

  val DiffsOutputDir = new File("target/diffs")
  val SnapshotsOutputDir = "target/snapshots"

  def generateDiffs(): IO[Unit] = timestamps.sliding(2).map {
    case List(initialTimestamp, updatedTimestamp) =>
      for {
        _ <- recreateDir(DiffsOutputDir)
        initial <- ZipSnapshotReader.read(s"$Root/${initialTimestamp}_BloodHound.zip")
        updated <- ZipSnapshotReader.read(s"$Root/${updatedTimestamp}_BloodHound.zip")
        _ <- initial.zip(updated).map { case (i, u) =>
          val diff = SnapshotDiff.from(i, u)

          SnapshotDiff.write(diff, s"target/diffs/$initialTimestamp-$updatedTimestamp-diff.json")
        }.getOrElse(().pure[IO])
      } yield ()
    case _ =>
      ().pure[IO]
  }.toList.sequence.map(_ => ())

  def evolution() = {
    timestamps.map { t =>
      ZipSnapshotReader.read(s"$Root/${t}_BloodHound.zip")
    }.sequence.map { snapshots =>
      DatabaseEvolution(snapshots.flatten)
    }
  }

  def recreateDir(dir: File) = IO.delay{
    FileUtils.deleteDirectory(dir)
    dir.mkdirs()
  }

  def produceSnapshots(): IO[Unit] = {
    for {
      _ <- recreateDir(new File(SnapshotsOutputDir))
      _ <- DatabaseEvolution.writeToDisk(DbGenerator.recreateRealDb(), s"$SnapshotsOutputDir/real")
      _ <- DatabaseEvolution.writeToDisk(DbGenerator.generateNestedGroupsDb(), s"$SnapshotsOutputDir/nested")
    } yield ()
  }

}
