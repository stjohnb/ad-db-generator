package net.bstjohn.ad.generator.generators

import io.circe.JsonObject

import scala.util.Random

object CommonGenerators {
  def generateSid(): String = ???


  def generateString(): String = ???

  def generateBoolean(): Boolean = Random.nextBoolean()

  def generateJsonObject(): JsonObject = JsonObject()


}
