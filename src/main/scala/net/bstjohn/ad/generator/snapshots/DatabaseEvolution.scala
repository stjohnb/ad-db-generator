package net.bstjohn.ad.generator.snapshots

case class DatabaseEvolution(
  snapshots: Iterable[DbSnapshot]
)
