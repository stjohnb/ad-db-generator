package net.bstjohn.ad.generator.format.common

import enumeratum._

sealed trait MetaType extends EnumEntry


object MetaType
  extends Enum[MetaType]
    with CirceEnum[MetaType] {
  val values: IndexedSeq[MetaType] = findValues

  case object computers extends MetaType
  case object containers extends MetaType
  case object domains extends MetaType
  case object gpos extends MetaType
  case object groups extends MetaType
  case object ous extends MetaType
  case object users extends MetaType

}