package net.bstjohn.ad.generator

import io.circe.Decoder
import io.circe.parser._
import io.circe.syntax._
import munit.CatsEffectSuite
import net.bstjohn.ad.generator.format.gpos.Gpos

class GposSpec extends CatsEffectSuite {

  test("serialises & de-serialises gpos") {

    for {
      serialised <- ResourceReader.read("20220509125013_gpos.json")
      parsed = parse(serialised).getOrElse(???)
      decoded = Decoder[Gpos].decodeJson(parsed).fold(e => throw e, identity)
    } yield {
      assertEquals(decoded.asJson, parsed.deepDropNullValues)
    }
  }
}
