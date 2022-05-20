package net.bstjohn.ad.generator.format.ace

import enumeratum._

sealed trait RightName extends EnumEntry

object RightName
  extends Enum[RightName]
    with CirceEnum[RightName] {
  val values: IndexedSeq[RightName] = findValues

  case object GenericAll extends RightName
  case object WriteDacl extends RightName
  case object WriteOwner extends RightName
  case object AllExtendedRights extends RightName
  case object Owns extends RightName
  case object GenericWrite extends RightName
  case object GetChanges extends RightName
  case object AddKeyCredentialLink extends RightName
  case object AddSelf extends RightName
  case object GetChangesAll extends RightName

}