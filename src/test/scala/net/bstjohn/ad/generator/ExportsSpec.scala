package net.bstjohn.ad.generator

import cats.implicits.toTraverseOps
import munit.CatsEffectSuite
import net.bstjohn.ad.generator.reader.ZipSnapshotReader

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._

class ExportsSpec extends CatsEffectSuite {

  test("de-serialises all exports") {
    val zipFiles = Files.find(Paths.get(
      "/Users/brendanstjohn/queens/ad-db-snapshots/"), 2, (p, _) => p.getFileName.toString.endsWith(".zip")
    ).iterator().asScala.toList

    for {
      snapshotOpts <- zipFiles.map(ZipSnapshotReader.read).sequence
    } yield {
      snapshotOpts.map { snapshotOpt =>
        assertEquals(snapshotOpt.isDefined, true)
      }
    }
  }
  test("de-serialises all generated snapshosts") {
    val zipFiles = Files.find(Paths.get(
      "target/snapshots"), 2, (p, _) => p.getFileName.toString.endsWith(".zip")
    ).iterator().asScala.toList

    for {
      snapshotOpts <- zipFiles.map(ZipSnapshotReader.read).sequence
    } yield {
      snapshotOpts.map { snapshotOpt =>
        assertEquals(snapshotOpt.isDefined, true)
      }
    }
  }
}
