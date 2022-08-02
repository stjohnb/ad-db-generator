package net.bstjohn.ad.preprocessing

import cats.effect.IO
import net.bstjohn.ad.generator.format.common.EntityId.{GroupId, UserId}
import net.bstjohn.ad.generator.format.computers.{Computer, LocalAdminType}
import net.bstjohn.ad.generator.format.groups.{Group, GroupMemberType}
import org.apache.commons.csv.{CSVFormat, CSVPrinter}

import java.io.FileWriter


case class GroupedUserChanges(
  userIds: Seq[UserId],
  userNames: Seq[String],
  groupsJoined: Int,
  groupsInherited: Int,
  acesReceived: Int,
  acesModified: Int,
  joinedDomainAdminsGroup: Boolean,
  userIsKerboroastable: Boolean,
  isLateralMovement: Boolean
) {

  def csvRow: Seq[String] = Seq(
    s"$groupsJoined",
    s"$groupsInherited",
    s"$acesReceived",
    s"$acesModified",
    if(joinedDomainAdminsGroup) "1" else "0",
    if(userIsKerboroastable) "1" else "0",
    if(isLateralMovement) "-1" else "1",
  ) // ++ userIds.map(_.value)

  def isChanged: Boolean = groupsJoined > 0 ||
    groupsInherited > 0 ||
    acesReceived > 0 ||
    acesModified > 0 ||
    isLateralMovement
}

object GroupedUserChanges {
  def from(
    userChanges: Seq[UserChanges],
    computers: Seq[Computer],
    groups: Seq[Group],
    groupsMap: Map[GroupId, Seq[GroupId]]
  ): Seq[GroupedUserChanges] = {
    val groupedUserIds: Seq[Seq[UserId]] = userChanges.map { userChange =>
      val loggedOnTo = computers.filter(_.allSessions.exists(s => s.UserSID == userChange.userId))
      val adminedByUsers: Seq[UserId] = loggedOnTo.flatMap(_.localAdmins.filter(_.ObjectType == LocalAdminType.User).map(_.ObjectIdentifier)).map(UserId(_))
      val adminedByGroups: Seq[GroupId] = loggedOnTo.flatMap(_.localAdmins.filter(_.ObjectType == LocalAdminType.Group).map(_.ObjectIdentifier)).map(GroupId(_))
      val allAdminedBy: Seq[UserId] = adminedByUsers ++ adminedByGroups.flatMap(allUsers(_, groupsMap, groups))

      val adminToComputers = loggedOnTo.filter(_.localAdmins.exists(a => a.ObjectType == LocalAdminType.User && a.ObjectIdentifier == userChange.userId.value))
      val adminToUsers = adminToComputers.flatMap(_.allSessions.map(_.UserSID))

      (allAdminedBy ++ adminToUsers).distinct
    }.distinct

    groupedUserIds.map { userIds =>
      val changes = userChanges.filter(c => userIds.contains(c.userId))

      GroupedUserChanges(
        userIds = userIds,
        userNames = Seq.empty,
        groupsJoined = changes.map(_.groupsJoined).sum,
        groupsInherited = changes.map(_.groupsInherited).sum,
        acesReceived = changes.map(_.acesReceived).sum,
        acesModified = changes.map(_.acesModified).sum,
        joinedDomainAdminsGroup = changes.exists(_.joinedDomainAdminsGroup),
        userIsKerboroastable = changes.exists(_.userIsKerboroastable),
        isLateralMovement = changes.exists(_.isLateralMovement)
      )
    }
  }


  val CsvHeader = Seq(
//    "userId",
    //    "userName",
    "groupsJoined",
    "groupsInherited",
    "acesReceived",
    "acesModified",
    "joinedDomainAdminsGroup",
    "userIsKerboroastable",
    "isNormalActivity"
  )


  def writeToDisk(changes: Seq[GroupedUserChanges], path: String): IO[Unit] = IO.delay {
    val printer = new CSVPrinter(new FileWriter(path), CSVFormat.DEFAULT)
    try {
      printer.printRecord(CsvHeader:_*)
      changes.foreach { change =>
        printer.printRecord(change.csvRow:_*)
      }
    } finally if (printer != null) printer.close()
  }


  private def allUsers(groupId: GroupId, groupsMap: Map[GroupId, Seq[GroupId]], allGroups: Seq[Group]): Seq[UserId] = {
    groupsMap.get(groupId) match {
      case Some(groupIds) =>
        val groups = allGroups.filter(g => groupIds.contains(g.ObjectIdentifier))
        val userMembers = groups.flatMap(_.Members.filter(_.ObjectType == GroupMemberType.User).map(_.ObjectIdentifier)).map(UserId(_))
        val groupMembers = groups.flatMap(_.Members.filter(_.ObjectType == GroupMemberType.Group).map(_.ObjectIdentifier)).map(GroupId(_))

        val nestedUserMembers = groupMembers.flatMap(groupId => allUsers(groupId, groupsMap, allGroups))
        userMembers ++ nestedUserMembers
      case None =>
        List.empty
    }
  }
}
