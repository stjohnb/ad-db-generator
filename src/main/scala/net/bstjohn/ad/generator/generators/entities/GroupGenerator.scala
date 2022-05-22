package net.bstjohn.ad.generator.generators.entities

import net.bstjohn.ad.generator.format.domains.Domain
import net.bstjohn.ad.generator.format.groups.{Group, GroupMember, GroupProperties}
import net.bstjohn.ad.generator.generators.common.CommonGenerators.{genBoolean, genOption, genSid, genString}
import net.bstjohn.ad.generator.generators.entities.AceGenerator.generateAces
import net.bstjohn.ad.generator.generators.model.EpochSeconds

object GroupGenerator {
  def generateGroup(
    domain: Domain,
    whenCreated: EpochSeconds,
    name: String = genString(),
    members: Iterable[GroupMember] = List.empty
  ): Group = {
    Group(
      members,
      generateAces(),
      genSid(),
      genBoolean(),
      genBoolean(),
      genGroupProperties(domain, whenCreated, name)
    )
  }

  private def genGroupProperties(
    domain: Domain,
    whenCreated: EpochSeconds,
    name: String
  ): GroupProperties = GroupProperties(
    domain = domain.Properties.name,
    name = name,
    distinguishedname = genOption().map(_ => genString()),
    domainsid = domain.ObjectIdentifier,
    highvalue = genOption().map(_ => genBoolean()),
    description = genOption().map(_ => genString()),
    whencreated = Some(whenCreated.value),
    admincount = genOption().map(_ => genBoolean()),
  )


}