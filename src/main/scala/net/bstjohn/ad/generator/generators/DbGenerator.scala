package net.bstjohn.ad.generator.generators

import net.bstjohn.ad.generator.format.DbSnapshot
import net.bstjohn.ad.generator.format.groups.{GroupMember, Groups}
import net.bstjohn.ad.generator.format.users.Users
import net.bstjohn.ad.generator.generators.MetaGenerator.generateMeta

object DbGenerator {
  def generateNestedGroupsDb(): DbSnapshot = {
    val users1 = (1 to 100).map(_ => UserGenerator.generateUser())
    val users2 = (1 to 100).map(_ => UserGenerator.generateUser())

    val group1 = GroupGenerator.generateGroup(
      members = users1.map(GroupMember.fromUser)
    )
    val group2 = GroupGenerator.generateGroup(
      members = users2.map(GroupMember.fromUser)
        :+ GroupMember.fromGroup(group1)
    )

    DbSnapshot(
      users = Users(
        users1 ++ users2,
        meta = generateMeta()
      ),
      groups = Groups(
        data = List(group1, group2),
        meta = generateMeta()
      )
    )
  }
}
