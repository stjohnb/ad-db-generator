package net.bstjohn.ad.generator.generators

import net.bstjohn.ad.generator.format.ace.{Ace, RightName}
import net.bstjohn.ad.generator.format.common.EntityId.{GroupId, UserId}
import net.bstjohn.ad.generator.format.groups.Group
import net.bstjohn.ad.generator.generators.entities.ComputerGenerator.generateComputers
import net.bstjohn.ad.generator.generators.entities.DomainGenerator.generateDomain
import net.bstjohn.ad.generator.generators.entities.GroupGenerator.{DomainAdminsGroupName, generateGroup}
import net.bstjohn.ad.generator.generators.entities.UserGenerator.{generateUser, generateUsers}
import net.bstjohn.ad.generator.generators.model.EpochSeconds
import net.bstjohn.ad.generator.snapshots.{DatabaseEvolution, DbSnapshot}

import java.util.{Calendar, GregorianCalendar}
import scala.annotation.unused
import scala.util.Random

class DynamicDb(name: String, scalingFactor: Int) {

  def scaled(range: Range): Range = Range(range.start * scalingFactor, range.end * scalingFactor)
  def sample(range: Range): Int = Random.nextInt(range.end - range.start) + range.start

  def evolution: Seq[DatabaseEvolution] = {
    println(s"DynamicDb($name, $scalingFactor)")
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

    val usersGroup = generateGroup(domain, s2.timestamp.plusMonths(1), "All users")

    val s3 = s2.updated(
      _.withUpdatedGroup(usersGroup)
        .withNewComputers(
          generateComputers(scaled(5 to 10), domain, s2.timestamp, s2.timestamp.plusMonths(1), Some(helpdeskGroup)))
        .randomSessions()
        .timestamp(s2.timestamp.plusMonths(1)))

    val s4Start = s3.timestamp.plusYears(1)
    val s4 = s3.updated(
      _.timestamp(s4Start.plusDays(10))
        .randomSessions())

    val s5 = (1 to 10).foldRight(s4.updated(_.clearLateralMovementIds))((_, evolution) => {
      addUsers(
        helpdeskManagersGroup.ObjectIdentifier,
        helpdeskGroup.ObjectIdentifier,
        domainAdminManagers.ObjectIdentifier,
        domainAdminsGroup.ObjectIdentifier,
        usersGroup.ObjectIdentifier,
        scaled(5 to 10),
        evolution,
        evolution.timestamp.plusMonths(1),
        evolution.timestamp.plusYears(1))})

    val s6 = s5.updated(
      _.randomSessions()
        .clearLateralMovementIds
        .timestamp(s5.timestamp.plusDays(1)))

    println(
      s"""${s6.computers.size} computers
         |${s6.users.size} users
         |${s6.groups.size} groups
         |${s6.computers.flatMap(_.allSessions).size} sessions
         |${s6.group(usersGroup.ObjectIdentifier).Members.size} users group users
         |${s6.group(helpdeskGroup.ObjectIdentifier).Members.size} helpdesk users
         |${s6.group(helpdeskManagersGroup.ObjectIdentifier).Members.size} helpdesk manager users
         |${s6.group(domainAdminsGroup.ObjectIdentifier).Members.size} domain admins
         |${s6.group(domainAdminManagers.ObjectIdentifier).Members.size} domain admin managers
         |""".stripMargin
    )

    s6.users.map { user =>
      println(s"escalatePrivileges")
      escalatePrivileges(user.ObjectIdentifier, s6, domainAdminsGroup, domainAdminManagers, helpdeskGroup, helpdeskManagersGroup)
    }.filter(_.latestSnapshot.lateralMovementIds.isDefined)
  }

  def escalatePrivileges(
    hackedUser: UserId,
    ev: DatabaseEvolution,
    domainAdminsGroup: Group,
    domainAdminManagersGroup: Group,
    helpdeskGroup: Group,
    helpdeskManagersGroup: Group
  ): DatabaseEvolution = {
    val userGroups = ev.groups
      .filter(_.Members.map(_.ObjectIdentifier).contains(hackedUser.value))

    val userGroupIds = userGroups.map(_.ObjectIdentifier)

    val localAdminComputers = ev.computers.filter { c =>
      c.localAdmins.exists(a => a.ObjectIdentifier == hackedUser.value) ||
        c.localAdmins.exists(a => userGroupIds.map(_.value).contains(a.ObjectIdentifier))
    }

    val canReachUsers: Seq[UserId] = localAdminComputers.flatMap { c =>
      c.allSessions.map(_.UserSID)
    }.distinct

    val canReachGroups: Seq[GroupId] = canReachUsers.flatMap { userId =>
      ev.groups.filter(_.Members.map(_.ObjectIdentifier).contains(userId.value)).map(_.ObjectIdentifier)
    }.distinct

    val inDomainAdmins = userGroupIds.contains(domainAdminsGroup.ObjectIdentifier)
    val inDomainAdminManagersGroup = userGroupIds.contains(domainAdminManagersGroup.ObjectIdentifier)
//    val inHelpDeskGroup = userGroupIds.contains(helpdeskGroup.ObjectIdentifier)
    val inHelpDeskManagersGroup = userGroupIds.contains(helpdeskManagersGroup.ObjectIdentifier)

    val canReachDomainAdmins = canReachGroups.contains(domainAdminsGroup.ObjectIdentifier)
    val canReachDomainAdminManagersGroup = canReachGroups.contains(domainAdminManagersGroup.ObjectIdentifier)
    val canReachHelpDeskManagersGroup = canReachGroups.contains(helpdeskManagersGroup.ObjectIdentifier)

//    if(inDomainAdmins || inDomainAdminManagersGroup || inHelpDeskGroup || canReachDomainAdmins || canReachDomainAdminManagersGroup || canReachHelpDeskManagersGroup ) {
//      println(
//        s"""
//           |userGroups: ${userGroups.map(_.Properties.name)}
//           |inDomainAdmins: $inDomainAdmins
//           |inDomainAdminManagersGroup: $inDomainAdminManagersGroup
//           |inHelpDeskGroup: $inHelpDeskGroup
//           |canReachDomainAdmins: $canReachDomainAdmins
//           |canReachDomainAdminManagersGroup: $canReachDomainAdminManagersGroup
//           |canReachHelpDeskManagersGroup: $canReachHelpDeskManagersGroup""".stripMargin)
//    }

    if(inDomainAdmins || canReachDomainAdmins) {
      // done. undetectable
      println(s"already domain admin - (inDomainAdmins: $inDomainAdmins, canReachDomainAdmins: $canReachDomainAdmins)")
      ev
    } else if(inDomainAdminManagersGroup || canReachDomainAdminManagersGroup) {
      // add self to domain admins
      println(s"Add self to domain admins - (inDomainAdminManagersGroup: $inDomainAdminManagersGroup, canReachDomainAdminManagersGroup: $canReachDomainAdminManagersGroup)")
      ev.updated(
        _.withUpdatedGroup(domainAdminsGroup.withGroupMember(hackedUser))
          .withLateralMovementIds(Seq(hackedUser)))
    } else if(inHelpDeskManagersGroup || canReachHelpDeskManagersGroup) {
      // add self to helpdesk group
      println(s"Add self to helpdesk - (inHelpDeskManagersGroup: $inHelpDeskManagersGroup, canReachHelpDeskManagersGroup: $canReachHelpDeskManagersGroup)")
      escalatePrivileges(
        hackedUser,
        ev.updated(
          _.withUpdatedGroup(helpdeskGroup.withGroupMember(hackedUser))
          .withLateralMovementIds(Seq(hackedUser))),
        domainAdminsGroup,
        domainAdminManagersGroup,
        helpdeskGroup,
        helpdeskManagersGroup
      )
    } else {
      // not hacked
      println("No hack")
      ev
    }
  }

  private def init(start: EpochSeconds, name: String): (Group, DatabaseEvolution) = {
    val domain = generateDomain(start)
//    val user = generateUser(domain, start)
    val domainAdminsGroup = generateGroup(domain, start, name = DomainAdminsGroupName)
//      .withGroupMember(user)

    (domainAdminsGroup, DatabaseEvolution(
      name,
      DbSnapshot(
        domain.withDomainAdminsGroup(domainAdminsGroup),
        List.empty, //(user),
        List(domainAdminsGroup),
        List.empty,
        start,
        Seq.empty)))
  }

  private def addUsers(
    helpdeskManagersGroup: GroupId,
    helpdeskGroup: GroupId,
    domainAdminManagers: GroupId,
    domainAdminsGroup: GroupId,
    usersGroup: GroupId,
    usersCount: Range,
    evolution: DatabaseEvolution,
    start: EpochSeconds,
    end: EpochSeconds,
    description: Option[String] = None
  ): DatabaseEvolution = {
    (0 to sample(usersCount)).foldRight(evolution)((_, evolution) =>
      addUser(
        helpdeskManagersGroup, helpdeskGroup, domainAdminManagers, domainAdminsGroup, usersGroup,
        evolution, start, end, description)
    )
  }

  private def addUser(
    helpdeskManagersGroup: GroupId,
    helpdeskGroup: GroupId,
    domainAdminManagers: GroupId,
    @unused domainAdminsGroup: GroupId,
    usersGroup: GroupId,
    evolution: DatabaseEvolution,
    start: EpochSeconds,
    end: EpochSeconds,
    description: Option[String]
  ): DatabaseEvolution = {
    val r = Random.nextDouble()

    val group = if(r < 0.05d) {
      domainAdminManagers
    } else if(r < 0.1d) {
      helpdeskManagersGroup
    } else if(r < 0.2d) {
      helpdeskGroup
    } else {
      usersGroup
    }

    val created = EpochSeconds(start.value + Random.nextLong(end.value - start.value))

    val user = generateUser(evolution.domain, created, description)
      .loggedOn(created)

    evolution.updated(
      _.withUpdatedUser(user)
      .withUpdatedGroup(
        evolution.groups.find(_.ObjectIdentifier == group)
          .getOrElse(???)
          .withGroupMember(user))
      .timestamp(evolution.timestamp.plusMinutes(1)))
  }

}

object DynamicDb {
  def apply(name: String, scalingFactor: Int): DynamicDb = {
    new DynamicDb(name, scalingFactor)
  }
}