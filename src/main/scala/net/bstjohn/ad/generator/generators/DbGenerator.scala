package net.bstjohn.ad.generator.generators

import net.bstjohn.ad.generator.format.ace.{Ace, RightName}
import net.bstjohn.ad.generator.format.groups.GroupMember
import net.bstjohn.ad.generator.format.users.User
import net.bstjohn.ad.generator.generators.entities.ComputerGenerator.{generateComputer, generateComputers}
import net.bstjohn.ad.generator.generators.entities.DomainGenerator.generateDomain
import net.bstjohn.ad.generator.generators.entities.GroupGenerator.{generateDomainAdminsGroup, generateGroup}
import net.bstjohn.ad.generator.generators.entities.UserGenerator.{generateUser, generateUsers, kerboroastableUser}
import net.bstjohn.ad.generator.generators.model.EpochSeconds
import net.bstjohn.ad.generator.snapshots.{DatabaseEvolution, DbSnapshot}

import java.util.{Calendar, GregorianCalendar}
import scala.util.Random._

object DbGenerator {
  def generateNestedGroupsDb(): DatabaseEvolution = {
    val date = new GregorianCalendar(2005, Calendar.FEBRUARY, 11).getTime
    val start = EpochSeconds.fromDate(date)

    val domain = generateDomain(start)
    val domainAdminsGroup = generateGroup(domain, start)
    val adminUser = generateUser(domain, start)
    val s1 = DbSnapshot(domain, Seq(adminUser), List(domainAdminsGroup), List(generateComputer(domain, start)), start, Seq.empty)

    val p2End = start.plusYears(2)
    val users1 = generateUsers(50 to 100, domain, start, p2End)
    val users2 = generateUsers(50 to 100, domain, start, p2End)

    val computers = generateComputers(100 to 200, domain, start, p2End)

    val group1 = generateGroup(domain, start, members = users1.map(GroupMember.fromUser))
    val group2 = generateGroup(domain, start,
      members = users2.map(GroupMember.fromUser)
        :+ GroupMember.fromGroup(group1))

    val s2 = s1
      .withUpdatedUsers(users1 ++ users2)
      .withUpdatedGroups(List(group1, group2))
      .withNewComputers(computers)
      .timestamp(p2End)

    DatabaseEvolution.from(s1, s2)
  }

  def recreateRealDb(): DatabaseEvolution = {
    val date = new GregorianCalendar(2005, Calendar.FEBRUARY, 11).getTime

    val start = EpochSeconds.fromDate(date)
    val domain = generateDomain(start)
    val domainAdminsGroup = generateDomainAdminsGroup(domain, start)
    val computers = generateComputers(50 to 100, domain, start, start.plusYears(2))

    val userCreated = start.plusHours(1)
    val userLoggedOn = start.plusHours(1)
    val bstjohn = kerboroastableUser(domain, userCreated).loggedOn(userLoggedOn)
    val s1 = DbSnapshot(domain, List(bstjohn), List(domainAdminsGroup), computers, start, Seq.empty)

    val groupManagers = generateGroup(domain, userCreated.plusMinutes(5), "Group managers",
      members = List(GroupMember.fromUser(bstjohn)))
    val s2 = s1
      .withUpdatedUser(bstjohn)
      .timestamp(userCreated.plusMinutes(10))

    val groupCreated = userCreated.plusMinutes(5)
    val csAgentsGroup = generateGroup(domain, groupCreated, "CS Agents")
      .withGroupMember(bstjohn)
    val s3 = s2
      .withUpdatedGroup(csAgentsGroup)
      .timestamp(groupCreated)

    val agentsStart = groupCreated.plusMonths(1)
    val agentsEnd = agentsStart.plusYears(1)
    val csAgents = generateUsers(50 to 100, domain, agentsStart, agentsEnd)
    val s4 = s3
      .withUpdatedUsers(csAgents)
      .withUpdatedGroup(csAgentsGroup.withGroupMembers(csAgents))
      .timestamp(agentsEnd.plusDays(1))

    val s5Start = agentsEnd.plusYears(1)
    val domainAdminManagers =
      generateGroup(domain, s5Start, "Domain admin managers")
        .withAces(Ace.forGroup(groupManagers, RightName.AddSelf))
        .withGroupMembers(shuffle(csAgents).take(3))

    val s5 = s4
      .withUpdatedGroup(domainAdminManagers)
      .withUpdatedGroup(domainAdminsGroup.withAces(Ace.forGroup(domainAdminManagers, RightName.GenericAll)))
      .timestamp(s5Start.plusDays(10))

    val s6Timestamp = s5Start.plusMonths(2)
    val s6 = s5
      .withUpdatedGroup(domainAdminManagers.withGroupMember(bstjohn))
      .withUpdatedGroup(domainAdminsGroup.withGroupMember(bstjohn))
      .timestamp(s6Timestamp)
      .withLateralMovementIds(Seq(bstjohn.ObjectIdentifier))

    DatabaseEvolution.from(s1, s2, s3, s4, s5, s6)
  }

  def geographicallyNestedGroups(): DatabaseEvolution = {
    val date = new GregorianCalendar(2005, Calendar.FEBRUARY, 11).getTime

    val start = EpochSeconds.fromDate(date)
    val domain = generateDomain(start)
    val domainAdminsGroup = generateGroup(domain, start, "DOMAIN ADMINS")
    val computers = generateComputers(50 to 60, domain, start, start.plusYears(2))

    val p1End = start.plusYears(1)
    val initialUsers = generateUsers(10 to 50, domain, start, p1End)
    val allUsersGroup = generateGroup(domain, start.plusMonths(1)).withGroupMembers(initialUsers)
    val managersGroup = generateGroup(domain, start.plusMonths(1)).withGroupMembers(shuffle(initialUsers).take(3))
    val s1 = DbSnapshot(domain, initialUsers, List(domainAdminsGroup, allUsersGroup, managersGroup), computers, start, Seq.empty)

    val p2End = p1End.plusYears(1)
    val dublinGroup = generateGroup(domain, p1End)
      .withGroupMembers(initialUsers)

    val belfastUsers = generateUsers(5 to 10, domain, p1End, p2End)
    val belfastGroup = generateGroup(domain, p1End)
      .withGroupMembers(belfastUsers)
    val belfastComputers = generateComputers(50 to 60, domain, start, p2End)

    val s2 = s1
      .withUpdatedUsers(belfastUsers)
      .withUpdatedGroup(dublinGroup)
      .withUpdatedGroup(belfastGroup)
      .withUpdatedGroup(managersGroup.withGroupMembers(shuffle(belfastUsers).take(1)))
      .withUpdatedGroup(
        allUsersGroup
          .withGroupMember(dublinGroup)
          .withGroupMember(belfastGroup))
      .withNewComputers(belfastComputers)
      .timestamp(p2End)

    val ev = (1 to 50).foldLeft(DatabaseEvolution.from(s1, s2))((evolution, i) => {
      val periodStart = p2End.plusWeeks(i - 1)
      val periodEnd = p2End.plusWeeks(i)
      val newBelfastUsers = generateUsers(0 to 2, domain, periodStart, periodEnd)
      val newDublinUsers = generateUsers(0 to 2, domain, periodStart, periodEnd)
      val newComputers = generateComputers(0 to 2, domain, periodStart, periodEnd)
      val newManagers: Seq[User] =
        if(nextBoolean() && nextBoolean()) shuffle(newBelfastUsers ++ newDublinUsers).take(1)
        else Seq.empty

      evolution.withSnapshot(
        evolution.latestSnapshot
          .withNewComputers(newComputers)
          .withUpdatedUsers(newBelfastUsers ++ newDublinUsers)
          .withUpdatedGroup(belfastGroup.withGroupMembers(newBelfastUsers))
          .withUpdatedGroup(dublinGroup.withGroupMembers(newDublinUsers))
          .withUpdatedGroup(managersGroup.withGroupMembers(newManagers))
          .timestamp(periodEnd)
      )
    })
    println(s"${ev.snapshots.size} snapshots")
    ev
  }
}
