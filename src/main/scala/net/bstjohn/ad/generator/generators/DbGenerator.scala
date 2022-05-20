package net.bstjohn.ad.generator.generators

import net.bstjohn.ad.generator.format.groups.{GroupMember, Groups}
import net.bstjohn.ad.generator.format.users.Users
import net.bstjohn.ad.generator.generators.DomainGenerator.generateDomain
import net.bstjohn.ad.generator.generators.MetaGenerator.generateMeta
import net.bstjohn.ad.generator.snapshots
import net.bstjohn.ad.generator.snapshots.DbSnapshot

object DbGenerator {
  def generateNestedGroupsDb(): DbSnapshot = {
    val domain = generateDomain()
    val domainAdminsGroup = GroupGenerator.generateGroup(domain)

    val users1 = (1 to 100).map(_ => UserGenerator.generateUser(domain))
    val users2 = (1 to 100).map(_ => UserGenerator.generateUser(domain))

    val group1 = GroupGenerator.generateGroup(
      domain = domain,
      members = users1.map(GroupMember.fromUser)
    )
    val group2 = GroupGenerator.generateGroup(
      domain = domain,
      members = users2.map(GroupMember.fromUser)
        :+ GroupMember.fromGroup(group1)
    )

    snapshots.DbSnapshot(
      None,
      users = Users(
        users1 ++ users2,
        meta = generateMeta()
      ),
      groups = Groups(
        data = List(group1, group2, domainAdminsGroup),
        meta = generateMeta()
      )
    )
  }
}
