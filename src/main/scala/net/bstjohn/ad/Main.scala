package net.bstjohn.ad

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {

  def run: IO[Unit] = for {
    _ <- Processes.generateScenarioSnapshots(100)
    _ <- Processes.generateTestEnvironmentDiffs
    _ <- Processes.generateScenarioDiffs
  } yield ()
}
