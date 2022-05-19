package net.bstjohn.ad.generator

import io.circe.Decoder
import io.circe.parser._
import io.circe.syntax._
import munit.CatsEffectSuite
import net.bstjohn.ad.generator.format.domains.Domains

class DomainsSpec extends CatsEffectSuite {

  test("serialises & de-serialises domains") {

    for {
      serialised <- ResourceReader.read("20220509125013_domains.json")
      parsed = parse(serialised).getOrElse(???)
      decoded = Decoder[Domains].decodeJson(parsed).fold(e => throw e, identity)
    } yield {
      assertEquals(decoded.asJson, parsed)
    }
  }
}
