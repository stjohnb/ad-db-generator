package net.bstjohn.ad.generator.generators.model

import scala.util.Random

object NameGenerator {
  val FirstNames = scala.io.Source.fromInputStream(
    getClass.getResourceAsStream(s"/first-names.txt")
  ).getLines().toList

  val LastNames = scala.io.Source.fromInputStream(
    getClass.getResourceAsStream(s"/last-names.txt")
  ).getLines().toList

  val CompanyNames = scala.io.Source.fromInputStream(
    getClass.getResourceAsStream(s"/company-names.txt")
  ).getLines().toList.map { name =>
    name
      .replaceAll("[^a-zA-Z0-9]", "-")
      .replaceAll("-----", "-")
      .replaceAll("----", "-")
      .replaceAll("---", "-")
      .replaceAll("--", "-")
  }

  val TLDs = Seq("com", "net", "local")

  def generateName(): Name = {
    Name(
      Random.shuffle(FirstNames).head,
      Random.shuffle(LastNames).head,
    )
  }

  def generateCompanyName(): String = {
    Random.shuffle(CompanyNames).head
  }

  def generateTLD(): String = {
    Random.shuffle(TLDs).head
  }
}
