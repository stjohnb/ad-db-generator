package net.bstjohn.ad.generator.generators

import net.bstjohn.ad.generator.format.domains.{Domain, DomainProperties}
import net.bstjohn.ad.generator.format.groups.{Group, GroupMember}
import net.bstjohn.ad.generator.generators.CommonGenerators.{genBoolean, genInt, genJsonObject, genOption, genSid, genString}

object DomainGenerator {
  def generateDomain(
  ): Domain = {
    val sid = genSid()
    val name = genString()

    Domain(
      List.empty,
      List.empty,
      List.empty,
      List.empty,
      sid,
      genBoolean(),
      genBoolean(),
      genJsonObject(),
      genDomainProperties(sid, name)
    )
  }

  private def genDomainProperties(sid: String, name: String): DomainProperties = {
    DomainProperties(
      domain = name,
      name = name,
      genString(),
      domainsid = sid,
      genBoolean(),
      genOption().map(_ => genString()),
      genInt(),
      genString(),
    )
  }

}
