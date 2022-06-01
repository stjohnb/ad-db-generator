package net.bstjohn.ad.generator.generators.entities

import io.circe.JsonObject
import net.bstjohn.ad.generator.format.computers.{Computer, ComputerProperties}
import net.bstjohn.ad.generator.format.domains.Domain
import net.bstjohn.ad.generator.generators.common.CommonGenerators._
import net.bstjohn.ad.generator.generators.entities.AceGenerator.generateAces
import net.bstjohn.ad.generator.generators.model.EpochSeconds

import scala.util.Random

object ComputerGenerator {
  def generateComputer(
    domain: Domain,
    whenCreated: EpochSeconds
  ): Computer = {
    Computer(
      PrimaryGroupSID = genOption().map(_ => genString()),
      AllowedToDelegate = List.empty,
      AllowedToAct = List.empty,
      HasSIDHistory = List.empty,
      Sessions = JsonObject(),
      PrivilegedSessions = JsonObject(),
      RegistrySessions = JsonObject(),
      LocalAdmins = JsonObject(),
      RemoteDesktopUsers = JsonObject(),
      DcomUsers = JsonObject(),
      PSRemoteUsers = JsonObject(),
      Status = None,
      Aces = generateAces(),
      ObjectIdentifier = genSid(),
      IsDeleted = genBoolean(),
      IsACLProtected = genBoolean(),
      Properties = genComputerProperties(domain, whenCreated)
    )
  }

  def generateComputers(
    randomCount: Range,
    domain: Domain,
    createdAfter: EpochSeconds,
    createdBefore: EpochSeconds
  ) = {
    val count = Random.nextInt(randomCount.end - randomCount.start) + randomCount.start

    (0 to count).map { _ =>
      generateComputer(
        domain,
        createdAfter.plusSeconds(Random.nextLong(createdBefore.value - createdAfter.value)))
    }
  }


  private def genComputerProperties(
    domain: Domain,
    whenCreated: EpochSeconds
  ): ComputerProperties = ComputerProperties(
    domain = domain.Properties.name,
    name = genString(),
    distinguishedname = genOption().map(_ => genString()),
    domainsid = domain.ObjectIdentifier,
    highvalue = genOption().map(_ => genBoolean()),
    description = genOption().map(_ => genString()),
    whencreated = Some(whenCreated.value),
    haslaps = genBoolean(),
    unconstraineddelegation = genOption().map(_ => genBoolean()),
    trustedtoauth = genOption().map(_ => genBoolean()),
    lastlogon = genOption().map(_ => genLong()),
    lastlogontimestamp = genOption().map(_ => genLong()),
    enabled = genOption().map(_ => genBoolean()),
    pwdlastset = genOption().map(_ => genLong()),
    serviceprincipalnames = None,
    operatingsystem = None,
    sidhistory = None
  )


}
