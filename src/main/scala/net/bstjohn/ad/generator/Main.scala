package net.bstjohn.ad.generator

import cats.effect.IOApp
import cats.effect.IO

object Main extends IOApp.Simple {

  def run: IO[Unit] = for {
    _ <- HelloWorld.generateDiffs()
    _ <- HelloWorld.produceSnapshots()
  } yield ()}
