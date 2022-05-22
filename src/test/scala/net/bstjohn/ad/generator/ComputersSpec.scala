package net.bstjohn.ad.generator

import io.circe.Decoder
import io.circe.parser._
import io.circe.syntax._
import munit.CatsEffectSuite
import net.bstjohn.ad.generator.format.computers.Computers

class ComputersSpec extends CatsEffectSuite {

  test("serialises & de-serialises computers") {

    for {
      serialised <- ResourceReader.read("20220509125013_computers.json")
      parsed = parse(serialised).getOrElse(???)
      decoded = Decoder[Computers].decodeJson(parsed).fold(e => throw e, identity)
    } yield {
      assertEquals(decoded.asJson, parsed.deepDropNullValues)
    }
  }
}
