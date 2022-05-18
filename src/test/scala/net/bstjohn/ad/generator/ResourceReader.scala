package net.bstjohn.ad.generator

import cats.effect.{IO, Resource}
import org.apache.commons.io.input.BOMInputStream

object ResourceReader {
  def read(filename: String): IO[String] = {
    Resource.make {
      IO(scala.io.Source.fromInputStream(
        new BOMInputStream(getClass.getResourceAsStream(s"/$filename")))
      )
    } { inStream =>
      IO(inStream.close()).handleErrorWith(_ => IO.unit) // release
    }.use { stream =>
      IO(stream.getLines().mkString)
    }
  }
}
