package net.bstjohn.ad.generator

import cats.effect.IO
import cats.implicits.{catsSyntaxApplicativeId, toTraverseOps}
import net.bstjohn.ad.generator.generators.DbGenerator
import net.bstjohn.ad.generator.reader.ZipSnapshotReader
import net.bstjohn.ad.generator.snapshots.{DatabaseEvolution, SnapshotDiff}
import org.apache.commons.io.FileUtils

import java.io.File

object Processes {

  val Root = "test-environment-snapshots"

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

  def generateDiffs(): IO[Unit] = for {
    _ <- recreateDir(DiffsOutputDir)
    _ <- timestamps.sliding(2).map {
      case List(initialTimestamp, updatedTimestamp) =>
        for {
          initial <- ZipSnapshotReader.read(s"$Root/${initialTimestamp}_BloodHound.zip")
          updated <- ZipSnapshotReader.read(s"$Root/${updatedTimestamp}_BloodHound.zip")
          _ <- initial.zip(updated).map { case (i, u) =>
            SnapshotDiff.write(SnapshotDiff.from(i, u), s"target/diffs/$initialTimestamp-$updatedTimestamp-diff.json")
          }.getOrElse(().pure[IO])
        } yield ()
      case _ =>
        ().pure[IO]
    }.toList.sequence
  } yield ()

  def produceSnapshots(): IO[Unit] = {
    for {
      _ <- recreateDir(new File(SnapshotsOutputDir))
      _ <- DatabaseEvolution.writeToDisk(DbGenerator.recreateRealDb(), s"$SnapshotsOutputDir/real")
      _ <- DatabaseEvolution.writeToDisk(DbGenerator.generateNestedGroupsDb(), s"$SnapshotsOutputDir/nested")
    } yield ()
  }

  private def recreateDir(dir: File) = IO.delay {
    FileUtils.deleteDirectory(dir)
    dir.mkdirs()
  }

}
