package net.bstjohn.ad.generator.snapshots

import enumeratum._

sealed trait SnapshotLabel extends EnumEntry

object SnapshotLabel
  extends Enum[SnapshotLabel]
    with CirceEnum[SnapshotLabel] {
  val values: IndexedSeq[SnapshotLabel] = findValues

  case object Normal extends SnapshotLabel
  case object Malicious extends SnapshotLabel

}