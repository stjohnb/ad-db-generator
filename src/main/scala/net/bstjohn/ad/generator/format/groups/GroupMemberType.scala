package net.bstjohn.ad.generator.format.groups

import enumeratum._

sealed trait GroupMemberType extends EnumEntry

object GroupMemberType
  extends Enum[GroupMemberType]
    with CirceEnum[GroupMemberType] {
  val values = findValues

  case object Group extends GroupMemberType
  case object User extends GroupMemberType
  case object Computer extends GroupMemberType

}