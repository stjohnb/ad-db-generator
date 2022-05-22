package net.bstjohn.ad.generator.generators.model

import scala.util.Random

object NameGenerator {
  val FirstNames = scala.io.Source.fromInputStream(
    getClass.getResourceAsStream(s"/first-names.txt")
  ).getLines().toList

  val LastNames = scala.io.Source.fromInputStream(
    getClass.getResourceAsStream(s"/last-names.txt")
  ).getLines().toList

  def generateName(): Name = {
    Name(
      Random.shuffle(FirstNames).head,
      Random.shuffle(LastNames).head,
    )
  }
}
