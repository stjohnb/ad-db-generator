package net.bstjohn.ad.generator.generators

import net.bstjohn.ad.generator.format.ace.{Ace, RightName}
import net.bstjohn.ad.generator.format.groups.GroupMember
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

    val users1 = (1 to 100).map(_ => generateUser(domain, start))
    val users2 = (1 to 100).map(_ => generateUser(domain, start))

    val group1 = generateGroup(
      domain = domain,
      whenCreated = start,
      members = users1.map(GroupMember.fromUser)
    )
    val group2 = generateGroup(
      domain = domain,
      whenCreated = start,
      members = users2.map(GroupMember.fromUser)
        :+ GroupMember.fromGroup(group1)
    )

    val s1 = DbSnapshot(
      domain,
      users1 ++ users2,
      List(group1, group2, domainAdminsGroup),
      start
    )

    DatabaseEvolution(s1)
  }

  def recreateRealDb(): DatabaseEvolution = {
    val date = new GregorianCalendar(2005, Calendar.FEBRUARY, 11).getTime

    val start = EpochSeconds.fromDate(date)
    val domain = generateDomain(start)
    val domainAdminsGroup = generateGroup(domain, start, "DOMAIN ADMINS")
    val s1 = DbSnapshot(domain, List.empty, List(domainAdminsGroup), start)

    val userCreated = start.plusHours(1)
    val userLoggedOn = start.plusHours(1)
    val bstjohn = generateUser(domain, userCreated).loggedOn(userLoggedOn)
    val groupManagers = generateGroup(domain, userCreated.plusMinutes(5), "Group managers",
      members = List(GroupMember.fromUser(bstjohn)))
    val s2 = DbSnapshot(domain, List(bstjohn), List(domainAdminsGroup), userCreated.plusMinutes(10))

    val groupCreated = userCreated.plusMinutes(5)
    val csAgentsGroup = generateGroup(domain, groupCreated, "CS Agents")
      .withGroupMember(bstjohn)
    val s3 = DbSnapshot(domain, List(bstjohn), List(domainAdminsGroup, csAgentsGroup), groupCreated)

    val agentsStart = groupCreated.plusMonths(1)
    val agentsEnd = agentsStart.plusYears(1)
    val csAgents = generateUsers(100, domain, agentsStart, agentsEnd)
    val s4CsAgentsGroup = csAgentsGroup.withGroupMembers(csAgents)
    val s4 = DbSnapshot(domain, bstjohn +: csAgents, List(domainAdminsGroup, s4CsAgentsGroup), agentsEnd.plusDays(1))

    val s5Start = agentsEnd.plusYears(1)
    val domainAdminManagers = generateGroup(domain, s5Start, "Domain admin managers",
      members = List(GroupMember.fromUser(bstjohn))
    ).withAces(Ace.forGroup(groupManagers, RightName.AddSelf))
    val s5DomainAdmins = domainAdminsGroup.withAces(Ace.forGroup(domainAdminManagers, RightName.GenericAll))
    val s5 = DbSnapshot(domain, bstjohn +: csAgents, List(s5DomainAdmins, s4CsAgentsGroup), s5Start.plusDays(10))

    DatabaseEvolution(s1, s2, s3, s4, s5)
  }
}
