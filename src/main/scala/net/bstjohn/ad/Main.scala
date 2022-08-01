package net.bstjohn.ad

import cats.effect.{IO, IOApp}
import net.bstjohn.ad.generator.reader.ZipSnapshotReader
import net.bstjohn.ad.generator.snapshots.DbSnapshot

object Main extends IOApp.Simple {

  def run: IO[Unit] = for {
    _ <- Processes.generateScenarioSnapshots
    _ <- Processes.generateTestEnvironmentDiffs
    _ <- Processes.generateScenarioDiffs
//    _ <- AdCsvImportGenerator.gen
  } yield ()

  def run0: IO[Unit] = for {
    read <- ZipSnapshotReader.read("/Users/brendanstjohn/queens/ad-db-snapshots/20220509121121_BloodHound.zip", None)
    _ <- DbSnapshot.writeToDisk(read.getOrElse(???), "target")
  } yield ()
}
