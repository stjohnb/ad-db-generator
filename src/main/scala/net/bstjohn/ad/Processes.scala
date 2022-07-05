package net.bstjohn.ad

import cats.effect.IO
import cats.implicits._
import net.bstjohn.ad.generator.format.common.EntityId.UserId
import net.bstjohn.ad.generator.generators.RecreateRealDb
import net.bstjohn.ad.generator.reader.ZipSnapshotReader
import net.bstjohn.ad.generator.snapshots.DatabaseEvolution
import net.bstjohn.ad.preprocessing.SnapshotDiff
import org.apache.commons.io.FileUtils

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters._

object Processes {

  val DiffsOutputDir = new File("target/diffs")
  val SnapshotsOutputDir = "target/snapshots"

  val generateScenarioSnapshots: IO[Unit] = {
    for {
      _ <- recreateDir(new File(SnapshotsOutputDir))
      _ <- recreateDir(new File("feature-vectors"))
      _ <- (1 to 20).flatMap(rand =>
        (1 to 10).map(run =>
          DatabaseEvolution.writeToDisk(
            RecreateRealDb.evolution(s"randomness-${rand}_run-$run", rand),
            SnapshotsOutputDir
          ))).toList.sequence
//        (1 to loopCount).map(i => Scenarios.nestedGroups(s"nested-groups-$i")) ++
//        (1 to loopCount).map(i => Scenarios.geographicallyNestedGroups(s"geographic-$i"))
//      _ <- scenarios.map(scenario => DatabaseEvolution.writeToDisk(scenario, SnapshotsOutputDir)).toList.sequence
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

    for {
      _ <- recreateDir(DiffsOutputDir)
      _ <- scenarios.map(generateDiffs).sequence
    } yield ()
  }

  private def generateDiffs(scenario: Path): IO[Seq[SnapshotDiff]] = {
    val files = Files.walk(scenario).iterator().asScala.toList
      .filter(_.toString.endsWith("zip"))
      .sortBy(p => p.getFileName.toString)

    val pairs: Seq[List[Path]] = files.sliding(2).toList
    val scenarioName = scenario.getFileName.toString

    for {
      _ <- recreateDir(new File(s"$DiffsOutputDir/$scenarioName"))
      diffOpts <- pairs.map {
        case List(initialName, updatedName) =>
          produceDiff((initialName, None), (updatedName, None))

        case _ => IO.pure(None)
      }.sequence
      diffs = diffOpts.flatten
//      _ <- diffs.map(SnapshotDiff.writeUserChanges(_, s"$DiffsOutputDir/$scenarioName")).sequence
      outputDir = s"feature-vectors/$scenarioName"
      _ <- recreateDir(new File(outputDir))
      _ <- SnapshotDiff.writeAllUserChanges(diffs, outputDir)
    } yield diffs
  }

  private def produceDiff(
    initialName: (Path, Option[Seq[UserId]]),
    updatedName: (Path, Option[Seq[UserId]])
  ): IO[Option[SnapshotDiff]] = {
    for {
      initial <- ZipSnapshotReader.read(initialName._1, initialName._2)
      updated <- ZipSnapshotReader.read(updatedName._1, updatedName._2)
    } yield {
      initial.zip(updated).map { case (i, u) =>
        SnapshotDiff.from(i, u)
      }
    }
  }

  private def recreateDir(dir: File): IO[Unit] = IO.delay {
    FileUtils.deleteDirectory(dir)
    dir.mkdirs()
    ()
  }

}
