package net.bstjohn.ad.generator.generators

import net.bstjohn.ad.generator.format.domains.Domain
import net.bstjohn.ad.generator.format.users.{User, UserProperties}
import net.bstjohn.ad.generator.generators.AceGenerator.generateAces
import net.bstjohn.ad.generator.generators.CommonGenerators.{genBoolean, genInt, genOption, genSid, genString}

object UserGenerator {
  def generateUser(
    domain: Domain
  ): User = {
    User(
      List.empty,
      None,
      List.empty,
      List.empty,
      generateAces(),
      genSid(),
      genBoolean(),
      genBoolean(),
      generateUserProperties(domain)
    )
  }

  def generateUserProperties(domain: Domain): UserProperties = {
    UserProperties(
      domain = domain.Properties.name,
      name = genString(),
      distinguishedname = genOption().map(_ => genString()),
      domainsid = domain.ObjectIdentifier,
      highvalue = genOption().map(_ => genBoolean()),
      description = genOption().map(_ => genString()),
      whencreated = genOption().map(_ => genInt()),
      sensitive = genOption().map(_ => genBoolean()),
      dontreqpreauth = genOption().map(_ => genBoolean()),
      passwordnotreqd = genOption().map(_ => genBoolean()),
      unconstraineddelegation = genOption().map(_ => genBoolean()),
      pwdneverexpires = genOption().map(_ => genBoolean()),
      enabled = genOption().map(_ => genBoolean()),
      trustedtoauth = genOption().map(_ => genBoolean()),
      lastlogon = genOption().map(_ => genInt()),
      lastlogontimestamp = genOption().map(_ => genInt()),
      pwdlastset = genOption().map(_ => genInt()),
      serviceprincipalnames = None ,
      hasspn = genOption().map(_ => genBoolean()),
      displayname = None,
      email = None,
      title = None,
      homedirectory = None,
      userpassword = None,
      unixpassword = None,
      unicodepassword = None,
      sfupassword = None,
      admincount = genOption().map(_ => genBoolean()),
      sidhistory = None,
    )
  }

}
