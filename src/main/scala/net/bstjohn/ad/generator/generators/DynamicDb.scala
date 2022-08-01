package net.bstjohn.ad.generator.generators

import net.bstjohn.ad.generator.format.ace.{Ace, RightName}
import net.bstjohn.ad.generator.format.common.EntityId.GroupId
import net.bstjohn.ad.generator.format.computers.LocalAdminType
import net.bstjohn.ad.generator.format.groups.{Group, GroupMember}
import net.bstjohn.ad.generator.generators.entities.ComputerGenerator.{generateComputer, generateComputers}
import net.bstjohn.ad.generator.generators.entities.DomainGenerator.generateDomain
import net.bstjohn.ad.generator.generators.entities.GroupGenerator.{DomainAdminsGroupName, generateGroup}
import net.bstjohn.ad.generator.generators.entities.UserGenerator.{generateUser, generateUsers}
import net.bstjohn.ad.generator.generators.model.EpochSeconds
import net.bstjohn.ad.generator.snapshots.{DatabaseEvolution, DbSnapshot}

import java.util.{Calendar, GregorianCalendar, UUID}
import scala.util.Random

object DynamicDb {

  def evolution(name: String, randomness: Int): DatabaseEvolution = {
    def scaled(range: Range): Range = Range(range.start * randomness, range.end * randomness)
    def sample(range: Range): Int = Random.nextInt(range.end - range.start) + range.start

    val date = new GregorianCalendar(2005, Calendar.FEBRUARY, 11).getTime
    val start = EpochSeconds.fromDate(date)
    val (domainAdminsGroup, s0) = init(start, name)
    val domain = s0.domain

    val s1End = start.plusMonths(2)

    val helpdeskManagersGroup =
      generateGroup(domain, s0.timestamp.plusMinutes(5), "Helpdesk managers")
        .withGroupMembers(Random.shuffle(s0.users).take(sample(scaled(1 to 2))))

    val helpdeskGroup =
      generateGroup(domain, s0.timestamp.plusMinutes(5), "Helpdesk")
        .withGroupMembers(Random.shuffle(s0.users).take(sample(scaled(1 to 2))))
        .withAces(Ace(helpdeskManagersGroup.ObjectIdentifier, RightName.GenericAll))

    val s1 = s0.updated(
      _.withUpdatedGroups(Seq(helpdeskManagersGroup, helpdeskGroup))
        .withUpdatedUsers(generateUsers(scaled(1 to 10), domain, start, s1End).map(_.loggedOn(s1End)))
        .randomSessions()
        .timestamp(s1End))

    val s2 = s1.updated(
      _.withNewComputers(generateComputers(scaled(1 to 10), domain, start, s1End, Some(helpdeskGroup)))
        .randomSessions()
        .timestamp(s1.timestamp.plusMinutes(10)))

    val usersGroup = generateGroup(domain, s2.timestamp.plusMonths(1))

    val s3 = s2.updated(
      _.withUpdatedGroup(usersGroup)
        .withNewComputers(
          generateComputers(scaled(5 to 10), domain, s2.timestamp, s2.timestamp.plusMonths(1), Some(helpdeskGroup)))
        .randomSessions()
        .timestamp(s2.timestamp.plusMonths(1)))

    val s4 = addUsers(
      usersGroup,
      scaled(5 to 10),
      s3,
      s3.timestamp.plusMonths(1),
      s3.timestamp.plusYears(1))
      .updated(_.randomSessions())

    val s5 = addAdminUser(s4, domainAdminsGroup).updated(_.randomSessions())

    val s6Start = s5.timestamp.plusYears(1)
    val s6 = s5.updated(
      _.timestamp(s6Start.plusDays(10))
        .randomSessions())

    val s7 = (1 to 10).foldRight(s6.updated(_.clearLateralMovementIds))((i, evolution) => {
      (if(i % 4 == 0) {
        newGroupAndUsers(evolution, scaled, helpdeskGroup)
      } else if(i % 2 == 0) {
        newUsersAndComputers(evolution, scaled, helpdeskGroup)
      } else {
        newGroupExistingUsers(evolution, scaled, helpdeskGroup)
      }).updated(_.randomSessions())
    })

    s7.updated(randomHack(_, helpdeskGroup, helpdeskManagersGroup))
  }

  def randomHack(
    snapshot: DbSnapshot,
    helpdeskGroup: Group,
    helpdeskManagersGroup: Group
  ): DbSnapshot = for {
    users <- snapshot.users
    computers <- snapshot.computers
    groups <- snapshot.groups
  } yield {
    val hackedComputer = Random.shuffle(computers.data.filter(_.Sessions.sessions.nonEmpty)).head
    val user = Random.shuffle(hackedComputer.Sessions.sessions).head

    val userGroups: Seq[GroupId] = groups.data.filter(_.Members.map(_.ObjectIdentifier).contains(user.userId.value)).map(_.ObjectIdentifier)

    val availableComputers = computers.data.filter { c =>
      c.localAdmins.exists(a => a.ObjectIdentifier == user.UserSID) ||
        c.localAdmins.exists(a => userGroups.map(_.value).contains(a.ObjectIdentifier))
    }

    val availableUsers = availableComputers.flatMap { c =>
      c.allSessions.map(_.userId)
    }

    val availableGroups: Seq[GroupId] = availableUsers.map { userId =>
      ???
    }
  }

  private def init(start: EpochSeconds, name: String): (Group, DatabaseEvolution) = {
    val domain = generateDomain(start)
    val user = generateUser(domain, start)
    val domainAdminsGroup = generateGroup(domain, start, name = DomainAdminsGroupName)
      .withGroupMember(user)

    (domainAdminsGroup, DatabaseEvolution(
      name,
      DbSnapshot(
        domain.withDomainAdminsGroup(domainAdminsGroup),
        List(user),
        List(domainAdminsGroup),
        List.empty,
        start,
        Seq.empty)))
  }

  private def newUsersAndComputers(
    evolution: DatabaseEvolution,
    random: Range => Range,
    helpdeskGroup: Group
  ): DatabaseEvolution = {
    val start = evolution.timestamp.plusMinutes(5)
    val end = evolution.timestamp.plusMonths(1)

    val newUsers = generateUsers(random(1 to 20), evolution.domain, start, end)
    val newComputers = generateComputers(random(1 to 10), evolution.domain, start, end, Some(helpdeskGroup))

    evolution.updated(
      _.withNewComputers(newComputers)
        .withUpdatedUsers(newUsers)
        .timestamp(evolution.timestamp.plusMonths(1)))
  }

  private def newGroupAndUsers(
    evolution: DatabaseEvolution,
    random: Range => Range,
    helpdeskGroup: Group
  ): DatabaseEvolution = {
    val start = evolution.timestamp.plusMinutes(5)
    val end = evolution.timestamp.plusMonths(1)

    val newUsers = generateUsers(random(1 to 20), evolution.domain, start, end)
    val newGroup = generateGroup(evolution.domain, start, s"Autogenerated group ${UUID.randomUUID()}")
      .withGroupMembers(newUsers)

    val newComputers = generateComputers(random(1 to 10), evolution.domain, start, end, Some(helpdeskGroup))
      .map(_.withAce(Ace(newGroup.ObjectIdentifier, RightName.GenericAll)))

    evolution.updated(
      _.withUpdatedGroup(newGroup)
        .withNewComputers(newComputers)
        .withUpdatedUsers(newUsers)
        .timestamp(evolution.timestamp.plusMonths(1)))
  }

  private def newGroupExistingUsers(
    evolution: DatabaseEvolution,
    random: Range => Range,
    helpdeskGroup: Group
  ): DatabaseEvolution = {
    val start = evolution.timestamp.plusMinutes(5)
    val end = evolution.timestamp.plusMonths(1)

    val newGroup = generateGroup(evolution.domain, start, s"Autogenerated group ${UUID.randomUUID()}")
      .withGroupMembers(evolution.users.filter(_ => Random.nextBoolean() && Random.nextBoolean()))

    val newComputers = generateComputers(random(1 to 10), evolution.domain, start, end, Some(helpdeskGroup))

    evolution.updated(
      _.withUpdatedGroup(newGroup)
        .withNewComputers(newComputers)
        .timestamp(evolution.timestamp.plusMonths(1)))
  }

  private def addUsers(
    group: Group,
    usersCount: Range,
    evolution: DatabaseEvolution,
    start: EpochSeconds,
    end: EpochSeconds,
    description: Option[String] = None
  ): DatabaseEvolution = {
    val users = generateUsers(usersCount, evolution.domain, start, end, description)

    evolution.updated(
      _.withUpdatedUsers(users)
      .withUpdatedGroup(group.withGroupMembers(users))
      .timestamp(end))
  }

  private def addAdminUser(
    evolution: DatabaseEvolution,
    domainAdminsGroup: Group
  ): DatabaseEvolution = {
    val newAdmin = generateUser(
      evolution.domain,
      evolution.timestamp.plusMonths(1),
      description = Some("new-admin"))

    evolution.updated(
      _.withUpdatedGroup(domainAdminsGroup.withGroupMember(newAdmin))
        .withUpdatedUser(newAdmin)
        .timestamp(evolution.timestamp.plusMonths(1)))
  }
}
