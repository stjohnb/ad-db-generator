package net.bstjohn.ad.generator.snapshots

case class EvolutionForks(
  scenarioName: String,
  latestSnapshot: DbSnapshot,
  previousSnapshots: Seq[DbSnapshot],
  finalForks: Seq[DbSnapshot]
) {
  def baseEvolution: DatabaseEvolution = DatabaseEvolution(
    scenarioName,
    latestSnapshot,
    previousSnapshots
  )
}

object EvolutionForks {
  def apply(evolution: DatabaseEvolution, finalForks: Seq[DbSnapshot]): EvolutionForks = {
    EvolutionForks(
      evolution.scenarioName,
      evolution.latestSnapshot,
      evolution.previousSnapshots,
      finalForks
    )
  }
}