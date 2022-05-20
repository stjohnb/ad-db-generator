package net.bstjohn.ad.generator

import cats.effect.IO
import cats.implicits.{catsSyntaxApplicativeId, toTraverseOps}
import net.bstjohn.ad.generator.reader.ZipSnapshotReader
import net.bstjohn.ad.generator.snapshots.SnapshotDiff

object HelloWorld {

  val Root = "/Users/brendanstjohn/queens/ad-db-snapshots/"

  val fileNames = List(
    "20220506180850_BloodHound.zip",
    "20220506183026_BloodHound.zip",
    "20220507084916_BloodHound.zip",
    "20220507091000_BloodHound.zip",
    "20220507092238_BloodHound.zip",
    "20220509120734_BloodHound.zip",
    "20220509121121_BloodHound.zip",
    "20220509121320_BloodHound.zip",
    "20220509121936_BloodHound.zip",
    "20220509125013_BloodHound.zip",
    "20220509135532_BloodHound.zip",
  )

  val filePaths = fileNames.map(name => s"$Root/$name").zipWithIndex

  def say(): IO[Unit] = filePaths.sliding(2).map {
    case List((initialPath, initialIndex), (updatedPath, updatedIndex)) =>
      for {
        initial <- ZipSnapshotReader.read(initialPath)
        updated <- ZipSnapshotReader.read(updatedPath)
        _ <- initial.zip(updated).map { case (i, u) =>
          val diff = SnapshotDiff.from(i, u)

          SnapshotDiff.write(diff, s"target/diffs/$initialIndex-$updatedIndex-diff.json")
        }.getOrElse(().pure[IO])
      } yield ()
    case _ =>
      ().pure[IO]
  }.toList.sequence.map(_ => ())

}
