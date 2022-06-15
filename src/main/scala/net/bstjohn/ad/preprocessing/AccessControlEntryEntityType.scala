package net.bstjohn.ad.preprocessing

import enumeratum._
import net.bstjohn.ad.generator.format.ace.AcePrincipalType

sealed trait AccessControlEntryEntityType extends EnumEntry

object AccessControlEntryEntityType
  extends Enum[AccessControlEntryEntityType]
    with CirceEnum[AccessControlEntryEntityType] {
    val values: IndexedSeq[AccessControlEntryEntityType] = findValues

    case object Computer extends AccessControlEntryEntityType
    case object Container extends AccessControlEntryEntityType
    case object Domain extends AccessControlEntryEntityType
    case object Gpo extends AccessControlEntryEntityType
    case object Group extends AccessControlEntryEntityType
    case object Ou extends AccessControlEntryEntityType
    case object User extends AccessControlEntryEntityType

  def apply(acePrincipalType: AcePrincipalType): AccessControlEntryEntityType = {
    acePrincipalType match {
      case AcePrincipalType.Group =>
        AccessControlEntryEntityType.Group
    }
  }
}
