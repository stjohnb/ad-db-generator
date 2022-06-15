package net.bstjohn.ad.preprocessing

import cats.effect.IO
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.bstjohn.ad.generator.format.common.EntityId.{GroupId, UserId}
import net.bstjohn.ad.generator.format.users.User
import net.bstjohn.ad.generator.generators.entities.GroupGenerator
import net.bstjohn.ad.preprocessing.diffs.GroupsDiff
import org.apache.commons.csv.{CSVFormat, CSVPrinter}

import java.io.FileWriter

case class UserChanges(
  userId: UserId,
  userName: String,
  groupsJoined: Int,
  groupsInherited: Int,
  acesReceived: Int,
  acesModified: Int,
  joinedDomainAdminsGroup: Boolean,
  userIsKerboroastable: Boolean,
  isLateralMovement: Boolean
) {
  def csvRow: Seq[String] = Seq(
    userId.value,
    userName,
    groupsJoined.toString,
    groupsInherited.toString,
    acesReceived.toString,
    acesModified.toString,
    if(joinedDomainAdminsGroup) "1" else "0",
    if(userIsKerboroastable) "1" else "0",
    if(isLateralMovement) "yes-lat" else "no-lat",
  )
}

object UserChanges {

  implicit val RelatedChangesDecoder: Decoder[UserChanges] = deriveDecoder[UserChanges]
  implicit val RelatedChangesEncoder: Encoder[UserChanges] = deriveEncoder[UserChanges]

  val CsvHeader = Seq(
    "userId",
    "userName",
    "groupsJoined",
    "groupsInherited",
    "acesReceived",
    "acesModified",
    "joinedDomainAdminsGroup",
    "userIsKerboroastable",
    "isLateralMovement"
  )

  def apply(
    user: User,
    groupDiffs: GroupsDiff,
    initialRelations: InvertedRelations,
    finalRelations: InvertedRelations,
    lateralMovementIds: Seq[UserId]
  ): UserChanges = {
    val groupsJoined: Seq[GroupsDiff.GroupUpdated] = groupDiffs.updated
      .filter(_.membersAdded.exists(_.ObjectIdentifier == user.ObjectIdentifier.value))

    val groupsInherited = groupsJoined.flatMap(g =>
      allGroupsRec(g.group.ObjectIdentifier, finalRelations.groupMemberships, Seq.empty))

    val acesGained = initialRelations.accessControlEntries.filter(ace => groupsInherited.exists(gid => gid.value == ace.sourceId))

    val initialUserAces = initialRelations.accessControlEntries.filter(e => e.sourceId == user.ObjectIdentifier.value).toSet
    val updatedUserAces = finalRelations.accessControlEntries.filter(e => e.sourceId == user.ObjectIdentifier.value).toSet

    UserChanges(
      userId = user.ObjectIdentifier,
      userName = user.Properties.name,
      groupsJoined = groupsJoined.size,
      groupsInherited = groupsInherited.size,
      acesReceived = acesGained.size,
      acesModified = (updatedUserAces -- initialUserAces).size,
      joinedDomainAdminsGroup = groupsJoined.exists(_.group.Properties.name == GroupGenerator.DomainAdminsGroupName),
      userIsKerboroastable = user.Properties.dontreqpreauth.contains(true),
      isLateralMovement = lateralMovementIds.contains(user.ObjectIdentifier)
    )
  }

  def writeToDisk(changes: Seq[UserChanges], path: String): IO[Unit] = IO.delay {
    val printer = new CSVPrinter(new FileWriter(path), CSVFormat.DEFAULT)
    try {
      printer.printRecord(CsvHeader:_*)
      changes.foreach { change =>
        printer.printRecord(change.csvRow:_*)
      }
    } finally if (printer != null) printer.close()
  }

  private def allGroupsRec(groupId: GroupId, groupsMap: Map[GroupId, Seq[GroupId]], acc: Seq[GroupId]): Seq[GroupId] = {
    groupsMap.get(groupId) match {
      case None =>
        acc
      case Some(groups) =>
        groups.foldLeft(acc)((acc, groupId) => allGroupsRec(groupId, groupsMap, acc))
    }
  }
}
