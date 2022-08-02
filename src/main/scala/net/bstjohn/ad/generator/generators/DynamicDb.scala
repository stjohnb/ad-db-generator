package net.bstjohn.ad.generator.generators

import net.bstjohn.ad.generator.format.ace.{Ace, RightName}
import net.bstjohn.ad.generator.format.common.EntityId.{GroupId, UserId}
import net.bstjohn.ad.generator.format.groups.{Group, GroupMemberType}
import net.bstjohn.ad.generator.format.users.User
import net.bstjohn.ad.generator.generators.entities.ComputerGenerator.generateComputers
import net.bstjohn.ad.generator.generators.entities.DomainGenerator.generateDomain
import net.bstjohn.ad.generator.generators.entities.GroupGenerator.{DomainAdminsGroupName, generateGroup}
import net.bstjohn.ad.generator.generators.entities.UserGenerator.{generateUser, generateUsers}
import net.bstjohn.ad.generator.generators.model.EpochSeconds
import net.bstjohn.ad.generator.snapshots.{DatabaseEvolution, DbSnapshot}

import java.util.{Calendar, GregorianCalendar}
import scala.util.Random

object DynamicDb {

  def evolution(name: String, scalingFactor: Int): Seq[DatabaseEvolution] = {
    def scaled(range: Range): Range = Range(range.start * scalingFactor, range.end * scalingFactor)
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

    val domainAdminManagers =
      generateGroup(domain, s0.timestamp.plusMinutes(5), "Domain admin managers")

    val s1 = s0.updated(
      _.withUpdatedGroups(Seq(helpdeskManagersGroup, helpdeskGroup, domainAdminManagers))
        .withUpdatedGroup(domainAdminsGroup.withAces(Ace(domainAdminManagers.ObjectIdentifier, RightName.GenericAll)))
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

    println(s"${s6.computers.size} computers, ${s6.users.size} users, ${s6.groups.size} groups, ${s6.computers.flatMap(_.allSessions).size} sessions")

    s6.users.zipWithIndex.map { case (user, index) =>
      escalatePrivileges(user, index, s6, domainAdminsGroup, domainAdminManagers, helpdeskGroup, helpdeskManagersGroup)
    }
  }

  def escalatePrivileges(
    hackedUser: User,
    index: Int,
    ev: DatabaseEvolution,
    domainAdminsGroup: Group,
    domainAdminManagersGroup: Group,
    helpdeskGroup: Group,
    helpdeskManagersGroup: Group
  ): DatabaseEvolution = {

    val userGroups = ev.groups
      .filter(_.Members.map(_.ObjectIdentifier).contains(hackedUser.ObjectIdentifier.value))
      .map(_.ObjectIdentifier)

    val localAdminComputers = ev.computers.filter { c =>
      c.localAdmins.exists(a => a.ObjectIdentifier == hackedUser.ObjectIdentifier.value) ||
        c.localAdmins.exists(a => userGroups.map(_.value).contains(a.ObjectIdentifier))
    }

    val availableUsers = localAdminComputers.flatMap { c =>
      c.allSessions.map(_.UserSID)
    }

    val availableGroups: Seq[GroupId] = availableUsers.flatMap { userId =>
      ev.groups.filter(_.Members.map(_.ObjectIdentifier).contains(userId.value)).map(_.ObjectIdentifier)
    }

    findPath(
      ev,
      hackedUser,
      domainAdminsGroup,
      domainAdminManagersGroup,
      helpdeskGroup,
      helpdeskManagersGroup,
      availableGroups
    ).updated(_.timestamp(ev.timestamp.plusDays(index)))
  }

  private def findPath(
    ev: DatabaseEvolution,
    hackedUser: User,
    domainAdminsGroup: Group,
    domainAdminManagersGroup: Group,
    helpdeskGroup: Group,
    helpdeskManagersGroup: Group,
    availableGroups: Seq[GroupId]): DatabaseEvolution = {
    if(availableGroups.contains(domainAdminsGroup.ObjectIdentifier)) {
      // done. undetectable
      println("already domain admin")
      ev
    } else if(availableGroups.contains(domainAdminManagersGroup.ObjectIdentifier)) {
      // add self to domain admins
      println(s"Add self to domain admins")
      ev.updated(
        _.withUpdatedGroup(domainAdminsGroup.withGroupMember(hackedUser))
          .withLateralMovementIds(Seq(hackedUser.ObjectIdentifier)))
    } else if(availableGroups.contains(helpdeskGroup.ObjectIdentifier)) {
      // search for a domain admin manager
      val domainAdminManagers = domainAdminManagersGroup.Members
        .filter(_.ObjectType == GroupMemberType.User)
        .map(_.ObjectIdentifier).map(UserId(_))

      if(ev.computers.exists(_.allSessions.exists(s => domainAdminManagers.contains(s.UserSID)))){
        println("Add self to domain admins via helpdesk -> domain admin managers")
        ev.updated(
          _.withUpdatedGroup(domainAdminsGroup.withGroupMember(hackedUser))
          .withLateralMovementIds(Seq(hackedUser.ObjectIdentifier)))
      } else {
        println(s"No domain admin managers")
        ev
      }
    } else if(availableGroups.contains(helpdeskManagersGroup.ObjectIdentifier)) {
      // add self to helpdesk group
      println(s"Add self to helpdesk")
      findPath(
        ev.updated(
          _.withUpdatedGroup(helpdeskGroup.withGroupMember(hackedUser))
          .withLateralMovementIds(Seq(hackedUser.ObjectIdentifier))),
        hackedUser,
        domainAdminsGroup,
        domainAdminManagersGroup,
        helpdeskGroup,
        helpdeskManagersGroup,
        availableGroups :+ helpdeskGroup.ObjectIdentifier
      )
    } else {
      // not hacked
      println("No hack")
      ev
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
