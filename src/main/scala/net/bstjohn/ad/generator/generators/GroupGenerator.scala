package net.bstjohn.ad.generator.generators

import net.bstjohn.ad.generator.format.groups.{Group, GroupMember}
import net.bstjohn.ad.generator.generators.CommonGenerators.{generateBoolean, generateJsonObject, generateSid}

object GroupGenerator {
  def generateGroup(
    members: Iterable[GroupMember] = List.empty
  ): Group = {
    Group(
      members,
      List.empty,
      generateSid(),
      generateBoolean(),
      generateBoolean(),
      generateJsonObject()
    )
  }

}
