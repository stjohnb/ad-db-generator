package net.bstjohn.ad.preprocessing

import enumeratum._
import net.bstjohn.ad.generator.format.ace.AcePrincipalType

sealed trait AccessControlEntryEntityType extends EnumEntry

object AccessControlEntryEntityType
  extends Enum[AccessControlEntryEntityType] {
    val values: IndexedSeq[AccessControlEntryEntityType] = findValues

    case object User extends AccessControlEntryEntityType
    case object Group extends AccessControlEntryEntityType

  def apply(acePrincipalType: AcePrincipalType): AccessControlEntryEntityType = {
    acePrincipalType match {
      case AcePrincipalType.Group =>
        AccessControlEntryEntityType.Group
    }
  }
}
