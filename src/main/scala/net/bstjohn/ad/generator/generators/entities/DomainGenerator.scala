package net.bstjohn.ad.generator.generators.entities

import net.bstjohn.ad.generator.format.domains.{Domain, DomainProperties}
import net.bstjohn.ad.generator.generators.common.CommonGenerators.{genBoolean, genJsonObject, genOption, genSid, genString}
import net.bstjohn.ad.generator.generators.model.EpochSeconds

object DomainGenerator {
  def generateDomain(
    whenCreated: EpochSeconds
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
      genDomainProperties(sid, name, whenCreated)
    )
  }

  private def genDomainProperties(
    sid: String,
    name: String,
    whenCreated: EpochSeconds,
  ): DomainProperties = {
    DomainProperties(
      domain = name,
      name = name,
      genString(),
      domainsid = sid,
      genBoolean(),
      genOption().map(_ => genString()),
      whencreated = whenCreated.value,
      genString(),
    )
  }

}
