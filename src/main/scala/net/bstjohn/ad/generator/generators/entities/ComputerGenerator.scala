package net.bstjohn.ad.generator.generators.entities

import io.circe.JsonObject
import io.circe.syntax.EncoderOps
import net.bstjohn.ad.generator.format.computers.{Computer, ComputerProperties, LocalAdmin, LocalAdmins, Sessions}
import net.bstjohn.ad.generator.format.domains.Domain
import net.bstjohn.ad.generator.format.groups.Group
import net.bstjohn.ad.generator.generators.common.CommonGenerators._
import net.bstjohn.ad.generator.generators.entities.AceGenerator.generateAces
import net.bstjohn.ad.generator.generators.model.EpochSeconds

import scala.util.Random

object ComputerGenerator {
  def generateComputer(
    domain: Domain,
    whenCreated: EpochSeconds,
    localAdminsGroup: Option[Group] = None
  ): Computer = {
    Computer(
      PrimaryGroupSID = genOption().map(_ => genString()),
      AllowedToDelegate = List.empty,
      AllowedToAct = List.empty,
      HasSIDHistory = List.empty,
      Sessions = genSessions(),
      PrivilegedSessions = genSessions(),
      RegistrySessions = genSessions(),
      LocalAdmins = genLocalAdmins(localAdminsGroup),
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
    createdBefore: EpochSeconds,
    localAdminsGroup: Option[Group] = None
  ) = {
    val count = Random.nextInt(randomCount.end - randomCount.start) + randomCount.start

    (0 to count).map { _ =>
      generateComputer(
        domain,
        EpochSeconds(createdAfter.value + Random.nextLong(createdBefore.value - createdAfter.value)),
        localAdminsGroup
      )
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

  private def genSessions() = Sessions(
    Results = genOption().map(_ => List.empty),
    Collected = genOption().map(_ => genBoolean()),
    FailureReason = genOption().map(_ => genJsonObject().asJson)
  )

  private def genLocalAdmins(localAdminsGroup: Option[Group]) = LocalAdmins(
    Results = localAdminsGroup.map(g => List(LocalAdmin.fromGroup(g))),
    Collected = genOption().map(_ => genBoolean()),
    FailureReason = genOption().map(_ => genJsonObject().asJson)
  )


}
