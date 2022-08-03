package net.bstjohn.ad.generator

import cats.implicits.toTraverseOps
import io.circe.Decoder
import io.circe.parser._
import io.circe.syntax._
import munit.CatsEffectSuite
import net.bstjohn.ad.generator.format.computers.Computers

class ComputersSpec extends CatsEffectSuite {

  test("serialises & de-serialises computers") {

    val files = List(
      "20220509125013_computers.json",
      "20220724175556_computers.json",
      "20220724175757_computers.json",
      "20220724175958_computers.json",
      "20220724180159_computers.json",
      "20220724180400_computers.json",
      "20220724180601_computers.json",
      "20220724180803_computers.json",
      "20220724181004_computers.json",
      "20220724181205_computers.json",
      "20220724181406_computers.json",
      "20220724181607_computers.json"
    )

    files.map { file =>
      for {
        serialised <- ResourceReader.read(file)
        parsed = parse(serialised).fold(e => throw e, identity)
        decoded = Decoder[Computers].decodeJson(parsed).fold(e => throw e, identity)
      } yield {
        assertEquals(decoded.asJson, parsed.deepDropNullValues)
      }
    }.sequence
  }
}
