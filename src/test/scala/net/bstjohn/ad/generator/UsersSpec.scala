package net.bstjohn.ad.generator

import io.circe.Decoder
import io.circe.parser._
import io.circe.syntax._
import munit.CatsEffectSuite
import net.bstjohn.ad.generator.format.users.Users

class UsersSpec extends CatsEffectSuite {

  test("serialises & de-serialises users") {

    for {
      serialised <- ResourceReader.read("20220509125013_users.json")
      parsed = parse(serialised).getOrElse(???)
      decoded = Decoder[Users].decodeJson(parsed).fold(e => throw e, identity)
    } yield {
      assertEquals(decoded.asJson, parsed)
    }
  }
}
