package net.bstjohn.ad.generator.generators.entities

import net.bstjohn.ad.generator.format.domains.Domain
import net.bstjohn.ad.generator.format.users.{User, UserProperties}
import net.bstjohn.ad.generator.generators.common.CommonGenerators.{genBoolean, genOption, genString, genUserId}
import net.bstjohn.ad.generator.generators.entities.AceGenerator.generateAces
import net.bstjohn.ad.generator.generators.model.EpochSeconds
import net.bstjohn.ad.generator.generators.model.NameGenerator.generateName

import scala.util.Random

object UserGenerator {
  def generateUser(
    domain: Domain,
    whenCreated: EpochSeconds,
    description: Option[String] = None
  ): User = {
    User(
      List.empty,
      None,
      List.empty,
      List.empty,
      generateAces(),
      genUserId(description),
      genBoolean(),
      genBoolean(),
      generateUserProperties(domain, whenCreated)
    )
  }

  def kerboroastableUser(
    domain: Domain,
    whenCreated: EpochSeconds,
    description: Option[String] = None
  ): User = {
    User(
      List.empty,
      None,
      List.empty,
      List.empty,
      generateAces(),
      genUserId(description),
      genBoolean(),
      genBoolean(),
      generateUserProperties(domain, whenCreated, dontreqpreauth = Some(true))
    )
  }

  def generateUsers(
    randomCount: Range,
    domain: Domain,
    createdAfter: EpochSeconds,
    createdBefore: EpochSeconds,
    description: Option[String] = None
  ) = {
    val count = Random.nextInt(randomCount.end - randomCount.start) + randomCount.start

    (0 to count).map { _ =>
      val created = EpochSeconds(createdAfter.value + Random.nextLong(createdBefore.value - createdAfter.value))

      generateUser(domain, created, description)
        .loggedOn(created)
    }
  }

  def generateUserProperties(
    domain: Domain,
    whenCreated: EpochSeconds,
    dontreqpreauth: Option[Boolean] = Some(false)
  ): UserProperties = {
    val name = generateName()

    UserProperties(
      domain = domain.Properties.name,
      name = s"${name.shortName}@${domain.Properties.name}",
      distinguishedname = genOption().map(_ => genString()),
      domainsid = domain.ObjectIdentifier,
      highvalue = genOption().map(_ => genBoolean()),
      description = genOption().map(_ => genString()),
      whencreated = Some(whenCreated.value),
      sensitive = genOption().map(_ => genBoolean()),
      dontreqpreauth = dontreqpreauth,
      passwordnotreqd = genOption().map(_ => genBoolean()),
      unconstraineddelegation = genOption().map(_ => genBoolean()),
      pwdneverexpires = genOption().map(_ => genBoolean()),
      enabled = genOption().map(_ => genBoolean()),
      trustedtoauth = genOption().map(_ => genBoolean()),
      lastlogon = None,
      lastlogontimestamp = None,
      pwdlastset = Some(whenCreated.value),
      serviceprincipalnames = None,
      hasspn = genOption().map(_ => genBoolean()),
      displayname = Some(name.fullName),
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
