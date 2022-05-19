package net.bstjohn.ad.generator

import io.circe.Decoder
import io.circe.parser._
import io.circe.syntax._
import munit.CatsEffectSuite
import net.bstjohn.ad.generator.format.groups.Groups

class GroupsSpec extends CatsEffectSuite {

  test("serialises & de-serialises groups") {

    for {
      serialised <- ResourceReader.read("20220509125013_groups.json")
      parsed = parse(serialised).getOrElse(???)
      decoded = Decoder[Groups].decodeJson(parsed).fold(e => throw e, identity)
    } yield {
//      decoded.data.foreach(g => g.Aces.foreach(g => println(g.PrincipalType)))
      assertEquals(decoded.asJson, parsed.deepDropNullValues)
    }
  }
}
