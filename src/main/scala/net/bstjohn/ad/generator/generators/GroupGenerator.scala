package net.bstjohn.ad.generator.generators

import net.bstjohn.ad.generator.format.domains.Domain
import net.bstjohn.ad.generator.format.groups.{Group, GroupMember, GroupProperties}
import net.bstjohn.ad.generator.generators.AceGenerator.generateAces
import net.bstjohn.ad.generator.generators.CommonGenerators.{genBoolean, genInt, genJsonObject, genOption, genSid, genString}

object GroupGenerator {
  def generateGroup(
    domain: Domain = DomainGenerator.generateDomain(),
    members: Iterable[GroupMember] = List.empty
  ): Group = {
    Group(
      members,
      generateAces(),
      genSid(),
      genBoolean(),
      genBoolean(),
      genGroupProperties(domain)
    )
  }

  def genGroupProperties(domain: Domain): GroupProperties = GroupProperties(
    domain = domain.Properties.name,
    name = genString(),
    distinguishedname = genOption().map(_ => genString()),
    domainsid = domain.ObjectIdentifier,
    highvalue = genOption().map(_ => genBoolean()),
    description = genOption().map(_ => genString()),
    whencreated = genOption().map(_ => genInt()),
    admincount = genOption().map(_ => genBoolean()),
  )


}
