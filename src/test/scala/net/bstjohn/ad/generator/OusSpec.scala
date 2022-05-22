package net.bstjohn.ad.generator

import io.circe.Decoder
import io.circe.parser._
import io.circe.syntax._
import munit.CatsEffectSuite
import net.bstjohn.ad.generator.format.gpos.Gpos
import net.bstjohn.ad.generator.format.ous.Ous

class OusSpec extends CatsEffectSuite {

  test("serialises & de-serialises ous") {

    for {
      serialised <- ResourceReader.read("20220509125013_ous.json")
      parsed = parse(serialised).getOrElse(???)
      decoded = Decoder[Ous].decodeJson(parsed).fold(e => throw e, identity)
    } yield {
      assertEquals(decoded.asJson, parsed.deepDropNullValues)
    }
  }
}
