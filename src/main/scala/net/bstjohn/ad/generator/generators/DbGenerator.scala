package net.bstjohn.ad.generator.generators

import net.bstjohn.ad.generator.format.ace.{Ace, RightName}
import net.bstjohn.ad.generator.format.groups.GroupMember
import net.bstjohn.ad.generator.generators.entities.ComputerGenerator.generateComputers
import net.bstjohn.ad.generator.generators.entities.DomainGenerator.generateDomain
import net.bstjohn.ad.generator.generators.entities.GroupGenerator.generateGroup
import net.bstjohn.ad.generator.generators.entities.UserGenerator.{generateUser, generateUsers}
import net.bstjohn.ad.generator.generators.model.EpochSeconds
import net.bstjohn.ad.generator.snapshots.{DatabaseEvolution, DbSnapshot}

import java.util.{Calendar, GregorianCalendar}

object DbGenerator {
  def generateNestedGroupsDb(): DatabaseEvolution = {
    val date = new GregorianCalendar(2005, Calendar.FEBRUARY, 11).getTime
    val start = EpochSeconds.fromDate(date)

    val domain = generateDomain(start)
    val domainAdminsGroup = generateGroup(domain, start)
    val users1 = generateUsers(50 to 100, domain, start, start.plusYears(2))
    val users2 = generateUsers(50 to 100, domain, start, start.plusYears(2))

    val computers = generateComputers(100 to 200, domain, start, start.plusYears(2))

    val group1 = generateGroup(domain, start, members = users1.map(GroupMember.fromUser))
    val group2 = generateGroup(domain, start,
      members = users2.map(GroupMember.fromUser)
        :+ GroupMember.fromGroup(group1))

    val s1 = DbSnapshot(domain, users1 ++ users2, List(group1, group2, domainAdminsGroup), computers, start)

    DatabaseEvolution(s1)
  }

  def recreateRealDb(): DatabaseEvolution = {
    val date = new GregorianCalendar(2005, Calendar.FEBRUARY, 11).getTime

    val start = EpochSeconds.fromDate(date)
    val domain = generateDomain(start)
    val domainAdminsGroup = generateGroup(domain, start, "DOMAIN ADMINS")
    val computers = generateComputers(50 to 100, domain, start, start.plusYears(2))

    val s1 = DbSnapshot(domain, List.empty, List(domainAdminsGroup), computers, start)

    val userCreated = start.plusHours(1)
    val userLoggedOn = start.plusHours(1)
    val bstjohn = generateUser(domain, userCreated).loggedOn(userLoggedOn)
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
    val domainAdminManagers = generateGroup(domain, s5Start, "Domain admin managers",
      members = List(GroupMember.fromUser(bstjohn))
    ).withAces(Ace.forGroup(groupManagers, RightName.AddSelf))

    val s5 = s4
      .withUpdatedGroup(domainAdminManagers)
      .withUpdatedGroup(domainAdminsGroup.withAces(Ace.forGroup(domainAdminManagers, RightName.GenericAll)))
      .timestamp(s5Start.plusDays(10))

    val s6Timestamp = s5Start.plusMonths(2)
    val s6 = s5
      .withUpdatedGroup(domainAdminManagers.withGroupMember(bstjohn))
      .timestamp(s6Timestamp)

    DatabaseEvolution(s1, s2, s3, s4, s5, s6)
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
    val s1 = DbSnapshot(domain, initialUsers, List(domainAdminsGroup, allUsersGroup), computers, start)

    val p2End = p1End.plusYears(1)
    val dublinGroup = generateGroup(domain, p1End)
      .withGroupMembers(initialUsers)
    val belfastUsers = generateUsers(50 to 100, domain, p1End, p2End)
    val belfastGroup = generateGroup(domain, p1End)
      .withGroupMembers(belfastUsers)
    val s2 = s1
      .withUpdatedUsers(belfastUsers)
      .withUpdatedGroup(dublinGroup)
      .withUpdatedGroup(belfastGroup)
      .withUpdatedGroup(
        allUsersGroup
          .withGroupMember(dublinGroup)
          .withGroupMember(belfastGroup))


    DatabaseEvolution(s1, s2)
  }
}
