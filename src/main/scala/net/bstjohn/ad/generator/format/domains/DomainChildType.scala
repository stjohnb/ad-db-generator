package net.bstjohn.ad.generator.format.domains

import enumeratum._

sealed trait DomainChildType extends EnumEntry


object DomainChildType
  extends Enum[DomainChildType]
    with CirceEnum[DomainChildType] {
  val values = findValues

  case object Container extends DomainChildType
  case object OU extends DomainChildType
  case object Computer extends DomainChildType

}