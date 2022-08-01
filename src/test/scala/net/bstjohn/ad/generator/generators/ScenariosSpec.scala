package net.bstjohn.ad.generator.generators

import munit.FunSuite
import net.bstjohn.ad.preprocessing.{InvertedRelations, SnapshotDiff}

class ScenariosSpec
  extends FunSuite {

  test("groupsJoined & acesReceived") {
    val evolution = Scenarios.recreateRealDb("test", 1)
    val latest = evolution.latestSnapshot
    val previous = evolution.previousSnapshots.lastOption.getOrElse(fail("2nd snapshot missing"))

    val diff = SnapshotDiff.from(previous, latest)

    val domainAdminsGroup = latest.groups.flatMap(_.domainAdminsGroup).getOrElse(fail("No domain admins group"))

    val inverted = InvertedRelations.from(latest)

    val domainAdminAces = inverted.accessControlEntries.filter(e => e.sourceId == domainAdminsGroup.ObjectIdentifier.value)
    assertEquals(domainAdminAces.length, 3)

    assertEquals(domainAdminsGroup.Members.exists(_.ObjectIdentifier.contains("attacker")), true)

    val changes = diff.userChanges.find(_.userId.value.contains("attacker")).getOrElse(fail("No attacker changes"))

    assertEquals(changes.groupsJoined, 2)
    assertEquals(changes.acesReceived, 3)
  }

  test("joining newly created groups") {
    val evolution = Scenarios.recreateRealDb("test", 1)
    val first = evolution.first.getOrElse(fail("no first snapshot"))
    val second = evolution.second.getOrElse(fail("no second snapshot"))

    val diff = SnapshotDiff.from(first, second)

    assertEquals(diff.userChanges.length, 1)

    val changes = diff.userChanges.head

    assertEquals(changes.groupsJoined, 1)
    assertEquals(changes.acesReceived, 2)
  }
}
