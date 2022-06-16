package net.bstjohn.ad.generator.generators

import munit.FunSuite
import net.bstjohn.ad.generator.format.common.EntityId.UserId
import net.bstjohn.ad.preprocessing.{InvertedRelations, SnapshotDiff}

class ScenariosSpec
  extends FunSuite {

  test("recreate real db scenario") {
    val attackerId = UserId("attacker-id-123")
    val evolution = Scenarios.recreateRealDb("test", attackerId)
    val latest = evolution.latestSnapshot
    val previous = evolution.previousSnapshots.lastOption.getOrElse(fail("2nd snapshot missing"))

    val diff = SnapshotDiff.from(previous, latest)

    val domainAdminsGroup = latest.groups.domainAdminsGroup.getOrElse(fail("No domain admins group"))

    val inverted = InvertedRelations.from(latest)

    val domainAdminAces = inverted.accessControlEntries.filter(e => e.sourceId == domainAdminsGroup.ObjectIdentifier.value)
    assertEquals(domainAdminAces.length, 3)

    assertEquals(domainAdminsGroup.Members.exists(_.ObjectIdentifier == attackerId.value), true)

    val changes = diff.userChanges.find(_.userId == attackerId).getOrElse(fail("No attacker changes"))

    assertEquals(changes.groupsJoined, 2)
    assertEquals(changes.acesReceived, 3)
  }
}
