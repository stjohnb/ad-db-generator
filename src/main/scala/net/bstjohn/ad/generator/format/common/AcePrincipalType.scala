package net.bstjohn.ad.generator.format.common

import enumeratum._

sealed trait AcePrincipalType extends EnumEntry

object AcePrincipalType
  extends Enum[AcePrincipalType]
    with CirceEnum[AcePrincipalType] {
  val values: IndexedSeq[AcePrincipalType] = findValues

  case object Group extends AcePrincipalType

}