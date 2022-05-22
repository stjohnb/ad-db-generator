package net.bstjohn.ad.generator

import io.circe.Decoder
import io.circe.parser._
import io.circe.syntax._
import munit.CatsEffectSuite
import net.bstjohn.ad.generator.format.containers.Containers

class ContainersSpec extends CatsEffectSuite {

  test("serialises & de-serialises containers") {

    for {
      serialised <- ResourceReader.read("20220509125013_containers.json")
      parsed = parse(serialised).getOrElse(???)
      decoded = Decoder[Containers].decodeJson(parsed).fold(e => throw e, identity)
    } yield {
      assertEquals(decoded.asJson, parsed.deepDropNullValues)
    }
  }
}
