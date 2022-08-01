package net.bstjohn.ad.generator.format.computers

import enumeratum._

sealed trait LocalAdminType extends EnumEntry

object LocalAdminType
  extends Enum[LocalAdminType]
    with CirceEnum[LocalAdminType] {

  val values = findValues

  case object Group extends LocalAdminType
  case object User extends LocalAdminType

}