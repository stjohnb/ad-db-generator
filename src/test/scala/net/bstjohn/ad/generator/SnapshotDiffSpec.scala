package net.bstjohn.ad.generator

import munit.CatsEffectSuite
import net.bstjohn.ad.generator.format.common.EntityId.UserId
import net.bstjohn.ad.generator.reader.ZipSnapshotReader
import net.bstjohn.ad.preprocessing.SnapshotDiff


class SnapshotDiffSpec extends CatsEffectSuite {

  test("de-serialises all exports") {
    val attackerId = UserId("S-1-5-21-2767398339-3403964288-3041356156-1114")

    for {
      initial <- ZipSnapshotReader.read("test-environment-snapshots/20220509125013_BloodHound.zip", None)
      updated <- ZipSnapshotReader.read("test-environment-snapshots/20220520162651_BloodHound.zip", Some(Seq(attackerId)))
    } yield {
      val diff = SnapshotDiff.from(
        initial.getOrElse(fail("no initial snapshot")),
        updated.getOrElse(fail("no update snapshot")))

      val change = diff.userChanges.find(_.userId == attackerId)

      assertEquals(diff.userChanges.size, 6)
      assertEquals(change.map(_.groupsJoined), Some(1))
    }
  }
}
