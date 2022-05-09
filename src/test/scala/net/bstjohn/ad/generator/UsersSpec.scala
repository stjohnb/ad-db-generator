package net.bstjohn.ad.generator

import cats.effect.{IO, Resource}
import io.circe.Decoder
import io.circe.parser._
import io.circe.generic.auto._
import io.circe.syntax._
import munit.CatsEffectSuite
import net.bstjohn.ad.generator.format.Users
import org.apache.commons.io.input.BOMInputStream

class UsersSpec extends CatsEffectSuite {

  test("serialises & de-serialises users") {

    for {
      serialised <- read("20220509125013_users.json")
      parsed = parse(serialised).getOrElse(???)
      decoded = Decoder[Users].decodeJson(parsed).fold(e => throw e, identity)
    } yield {
      assertEquals(decoded.asJson, parsed)
    }
  }



  private def read(filename: String): IO[String] = {
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
