package net.bstjohn.ad.generator

import cats.effect.IO
import cats.implicits.{catsSyntaxApplicativeId, toTraverseOps}
import net.bstjohn.ad.generator.format.common.EntityId.UserId
import net.bstjohn.ad.generator.generators.Scenarios
import net.bstjohn.ad.generator.reader.ZipSnapshotReader
import net.bstjohn.ad.generator.snapshots.DatabaseEvolution
import net.bstjohn.ad.preprocessing.{SnapshotDiff, UserChanges}
import org.apache.commons.io.FileUtils

import scala.jdk.CollectionConverters._
import java.io.File
import java.nio.file.{Files, Path, Paths}

object Processes {

  val DiffsOutputDir = new File("target/diffs")
  val SnapshotsOutputDir = "target/snapshots"

  val generateScenarioSnapshots: IO[Unit] = {
    for {
      _ <- recreateDir(new File(SnapshotsOutputDir))
      _ <- DatabaseEvolution.writeToDisk(Scenarios.recreateRealDb(), s"$SnapshotsOutputDir/real")
      _ <- DatabaseEvolution.writeToDisk(Scenarios.nestedGroups(), s"$SnapshotsOutputDir/nested")
      _ <- DatabaseEvolution.writeToDisk(Scenarios.geographicallyNestedGroups(), s"$SnapshotsOutputDir/geographic")
    } yield ()
  }

  val generateTestEnvironmentDiffs: IO[Unit] = {
    val Root = "test-environment-snapshots"
    val scenarioName = "test-environment"

    val timestamps: Seq[(String, Option[Seq[UserId]])] = List(
      ("20220506180850", Some(Seq.empty)),
      ("20220506183026", Some(Seq.empty)),
      ("20220507084916", Some(Seq.empty)),
      ("20220507091000", Some(Seq.empty)),
      ("20220507092238", Some(Seq.empty)),
      ("20220509120734", Some(Seq.empty)),
      ("20220509121121", Some(Seq.empty)),
      ("20220509121320", Some(Seq.empty)),
      ("20220509121936", Some(Seq.empty)),
      ("20220509125013", Some(Seq.empty)),
//      ("20220509135532", Some(Seq.empty)),
      ("20220520162651", Some(Seq(UserId("S-1-5-21-2767398339-3403964288-3041356156-1114")))),
    )

    for {
      _ <- recreateDir(new File(s"$DiffsOutputDir/$scenarioName"))
      _ <- timestamps.sliding(2).map {
        case List(initialName, updatedName) =>
          produceDiff(
            scenarioName,
            (Paths.get(s"$Root/${initialName._1}_BloodHound.zip"), initialName._2),
            (Paths.get(s"$Root/${updatedName._1}_BloodHound.zip"), updatedName._2)
          )
        case _ =>
          ().pure[IO]
      }.toList.sequence
    } yield ()
  }

  val generateScenarioDiffs = IO.defer {
    val scenarios = Files.walk(Paths.get(SnapshotsOutputDir)).iterator().asScala.toList
      .filter(Files.isDirectory(_))
      .filter(_.toString != SnapshotsOutputDir)

    scenarios.map { scenario =>
      val files = Files.walk(scenario).iterator().asScala.toList
        .filter(_.toString.endsWith("zip"))
        .sortBy(p => p.getFileName.toString)
        .sliding(2).toList

      val scenarioName = scenario.getFileName.toString

      for {
        _ <- recreateDir(new File(s"$DiffsOutputDir/$scenarioName"))
        _ <- files.map {
          case List(initialName, updatedName) =>
            produceDiff(scenarioName, (initialName, None), (updatedName, None))
          case _ =>
            ().pure[IO]
        }.sequence
      } yield ()
    }.sequence
  }

  private def produceDiff(
    scenarioName: String,
    initialName: (Path, Option[Seq[UserId]]),
    updatedName: (Path, Option[Seq[UserId]])
  ): IO[Unit] = {
    for {
      initial <- ZipSnapshotReader.read(initialName._1, initialName._2)
      updated <- ZipSnapshotReader.read(updatedName._1, updatedName._2)
      _ <- initial.zip(updated).map { case (i, u) =>
        val diff = SnapshotDiff.from(i, u)

        for {
          _ <- SnapshotDiff.write(diff, s"$DiffsOutputDir/$scenarioName/${i.epoch.value}-${u.epoch.value}-diff.json")
          _ <- UserChanges.writeToDisk(diff.userChanges, s"$DiffsOutputDir/$scenarioName/${i.epoch.value}-${u.epoch.value}-changes.csv")
        } yield ()
      }.sequence
    } yield ()
  }

  private def recreateDir(dir: File): IO[Unit] = IO.delay {
    FileUtils.deleteDirectory(dir)
    dir.mkdirs()
    ()
  }

}
