package net.bstjohn.ad.generator

import cats.effect.IO
import cats.implicits.{catsSyntaxApplicativeId, toTraverseOps}
import net.bstjohn.ad.generator.generators.DbGenerator
import net.bstjohn.ad.generator.reader.ZipSnapshotReader
import net.bstjohn.ad.generator.snapshots.{DatabaseEvolution, SnapshotLabel}
import net.bstjohn.ad.preprocessing.SnapshotDiff
import org.apache.commons.io.FileUtils

import java.io.File

object Processes {

  val Root = "test-environment-snapshots"

  val timestamps = List(
    ("20220506180850", SnapshotLabel.Normal),
    ("20220506183026", SnapshotLabel.Normal),
//    ("20220507084916", SnapshotLabel.Normal),
//    ("20220507091000", SnapshotLabel.Normal),
//    ("20220507092238", SnapshotLabel.Normal),
//    ("20220509120734", SnapshotLabel.Normal),
//    ("20220509121121", SnapshotLabel.Normal),
//    ("20220509121320", SnapshotLabel.Normal),
////    ("20220509121936", SnapshotLabel.Normal),
//    ("20220509125013", SnapshotLabel.Normal),
//    ("20220509135532", SnapshotLabel.Normal),
//    ("20220520162651", SnapshotLabel.Malicious),
  )

  val DiffsOutputDir = new File("target/diffs")
  val SnapshotsOutputDir = "target/snapshots"

  def generateDiffs(): IO[Unit] = for {
    _ <- recreateDir(DiffsOutputDir)
    _ <- timestamps.sliding(2).map {
      case List(initialName, updatedName) =>
        for {
          initial <- ZipSnapshotReader.read(s"$Root/${initialName._1}_BloodHound.zip", initialName._2)
          updated <- ZipSnapshotReader.read(s"$Root/${updatedName._1}_BloodHound.zip", updatedName._2)
          _ <- initial.zip(updated).map { case (i, u) =>
            SnapshotDiff.write(SnapshotDiff.from(i, u), s"target/diffs/${initialName._1}-${updatedName._1}-diff.json")
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
