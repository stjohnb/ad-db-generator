package net.bstjohn.ad.generator

import cats.effect.IOApp
import cats.effect.IO

object Main extends IOApp.Simple {

  def run: IO[Unit] = for {
    _ <- Processes.generateDiffs()
    _ <- Processes.produceSnapshots()
  } yield ()}
