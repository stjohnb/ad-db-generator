package net.bstjohn.ad.generator.generators

import net.bstjohn.ad.generator.format.ace.{Ace, RightName}
import net.bstjohn.ad.generator.format.groups.GroupMember
import net.bstjohn.ad.generator.format.users.User
import net.bstjohn.ad.generator.generators.entities.ComputerGenerator.{generateComputer, generateComputers}
import net.bstjohn.ad.generator.generators.entities.DomainGenerator.generateDomain
import net.bstjohn.ad.generator.generators.entities.GroupGenerator._
import net.bstjohn.ad.generator.generators.entities.UserGenerator.{generateUser, generateUsers}
import net.bstjohn.ad.generator.generators.model.EpochSeconds
import net.bstjohn.ad.generator.snapshots.{DatabaseEvolution, DbSnapshot}

import java.util.{Calendar, GregorianCalendar}
import scala.util.Random._

object Scenarios {
  def nestedGroups(name: String): DatabaseEvolution = {
    val date = new GregorianCalendar(2005, Calendar.FEBRUARY, 11).getTime
    val start = EpochSeconds.fromDate(date)

    val domain = generateDomain(start)
    val domainAdminsGroup = generateGroup(domain, start, name = DomainAdminsGroupName)
    val adminUser = generateUser(domain, start)
    val s1 = DbSnapshot(
      domain.withDomainAdminsGroup(domainAdminsGroup),
      Seq(adminUser),
      List(domainAdminsGroup),
      List(generateComputer(domain, start)),
      start, Seq.empty)

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

    DatabaseEvolution.from(name, s1, s2)
  }

  def recreateRealDb(name: String, randomness: Int): DatabaseEvolution = {
    def random(range: Range): Range = Range(range.start * randomness, range.end * randomness)
    val date = new GregorianCalendar(2005, Calendar.FEBRUARY, 11).getTime

    val start = EpochSeconds.fromDate(date)
    val domain = generateDomain(start)
    val domainAdminsGroup = generateGroup(domain, start, name = DomainAdminsGroupName)
    val computers = generateComputers(random(50 to 100), domain, start, start.plusYears(2))

    val userCreated = start.plusHours(1)
//    val attacker = if(attackerIsKerboroastable) kerboroastableUser(domain, userCreated, description = Some("attacker-kerb"))
//    else generateUser(domain, userCreated, description = Some("attacker-non-kerb"))
    val attacker = generateUser(domain, userCreated, description = Some("attacker"))
    val s1Timestamp = start.plusHours(2)

    val s1 = DbSnapshot(
      domain.withDomainAdminsGroup(domainAdminsGroup),
      List(attacker.loggedOn(userCreated)),
      List(domainAdminsGroup),
      computers,
      s1Timestamp, Seq.empty)

    val groupManagers = generateGroup(domain, s1Timestamp.plusMinutes(5), "Group managers",
      members = List(GroupMember.fromUser(attacker)))
    val managementComputer = generateComputer(domain, s1Timestamp.plusMinutes(5))
      .withAces(Seq(
        Ace(groupManagers.ObjectIdentifier, RightName.GenericAll),
        Ace(groupManagers.ObjectIdentifier, RightName.Owns)))

    val s2Timestamp = s1Timestamp.plusMinutes(10)
    val s2 = s1
      .withUpdatedGroup(groupManagers)
      .withNewComputer(managementComputer)
      .timestamp(s2Timestamp)

    val s3Timestamp = s2Timestamp.plusMinutes(5)
    val csAgentsGroup = generateGroup(domain, s3Timestamp, "CS Agents")
      .withGroupMember(attacker)
    val csAgentsComputer = generateComputer(domain, s3Timestamp).withAce(Ace(csAgentsGroup.ObjectIdentifier, RightName.GenericAll))
    val s3 = s2
      .withUpdatedGroup(csAgentsGroup)
      .withNewComputer(csAgentsComputer)
      .timestamp(s3Timestamp)

    val agentsStart = s3Timestamp.plusMonths(1)
    val agentsEnd = agentsStart.plusYears(1)
    val csAgents = generateUsers(random(5 to 10), domain, agentsStart, agentsEnd, description = Some("csAgent"))
    val s4Timestamp = agentsEnd.plusDays(1)
    val s4 = s3
      .withUpdatedUsers(csAgents)
      .withUpdatedGroup(csAgentsGroup.withGroupMembers(csAgents))
      .timestamp(s4Timestamp)

    val s5Start = s4Timestamp.plusMonths(1)
    val newAdmin = generateUser(domain, s5Start, description = Some("new-admin"))
    val s5 = s4
      .withUpdatedGroup(domainAdminsGroup.withGroupMember(newAdmin))
      .withUpdatedUser(newAdmin)
      .timestamp(s5Start)

    val s6Start = s5Start.plusYears(1)
    val domainAdminManagers =
      generateGroup(domain, s6Start, "Domain admin managers")
        .withAces(Ace(groupManagers.ObjectIdentifier, RightName.AddSelf))
        .withGroupMembers(shuffle(csAgents).take(3))

    val s6Timestamp = s6Start.plusDays(10)
    val s6 = s5
      .withUpdatedGroup(domainAdminManagers)
      .withUpdatedGroup(domainAdminsGroup.withAces(Ace(domainAdminManagers.ObjectIdentifier, RightName.GenericAll)))
      .timestamp(s6Timestamp)

    val s7Timestamp = s6Timestamp.plusMonths(2)
    val s7 = s6
      .withUpdatedGroup(domainAdminManagers.withGroupMember(attacker))
      .withUpdatedGroup(domainAdminsGroup.withGroupMember(attacker))
      .withLateralMovementIds(Seq(attacker.ObjectIdentifier))
      .timestamp(s7Timestamp)

    DatabaseEvolution.from(name, s1, s2, s3, s4, s5, s6, s7)
  }

  def geographicallyNestedGroups(name: String): DatabaseEvolution = {
    val date = new GregorianCalendar(2005, Calendar.FEBRUARY, 11).getTime

    val start = EpochSeconds.fromDate(date)
    val domain = generateDomain(start)
    val domainAdminsGroup = generateGroup(domain, start, DomainAdminsGroupName)
    val computers = generateComputers(50 to 60, domain, start, start.plusYears(2))

    val p1End = start.plusYears(1)
    val initialUsers = generateUsers(10 to 50, domain, start, p1End)
    val allUsersGroup = generateGroup(domain, start.plusMonths(1)).withGroupMembers(initialUsers)
    val managersGroup = generateGroup(domain, start.plusMonths(1)).withGroupMembers(shuffle(initialUsers).take(3))
    val s1 = DbSnapshot(
      domain.withDomainAdminsGroup(domainAdminsGroup),
      initialUsers,
      List(domainAdminsGroup, allUsersGroup, managersGroup),
      computers, start, Seq.empty)

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

    (1 to 50).foldLeft(DatabaseEvolution.from(name, s1, s2))((evolution, i) => {
      val periodStart = p2End.plusWeeks(i - 1)
      val periodEnd = p2End.plusWeeks(i)
      val newBelfastUsers = generateUsers(0 to 5, domain, periodStart, periodEnd)
      val newDublinUsers = generateUsers(0 to 5, domain, periodStart, periodEnd)
      val newComputers = generateComputers(0 to (newDublinUsers.size + newBelfastUsers.size), domain, periodStart, periodEnd)
        .map(_.withAce(Ace(allUsersGroup.ObjectIdentifier, RightName.GenericAll)))
      val newManagers: Seq[User] =
        if(nextBoolean() && nextBoolean()) shuffle(newBelfastUsers ++ newDublinUsers).take(1)
        else Seq.empty

      evolution.updated(
        _.withNewComputers(newComputers)
          .withUpdatedUsers(newBelfastUsers ++ newDublinUsers)
          .withUpdatedGroup(belfastGroup.withGroupMembers(newBelfastUsers))
          .withUpdatedGroup(dublinGroup.withGroupMembers(newDublinUsers))
          .withUpdatedGroup(allUsersGroup.withGroupMembers(newDublinUsers ++ newBelfastUsers))
          .withUpdatedGroup(managersGroup.withGroupMembers(newManagers))
          .timestamp(periodEnd)
      )
    })
  }
}
