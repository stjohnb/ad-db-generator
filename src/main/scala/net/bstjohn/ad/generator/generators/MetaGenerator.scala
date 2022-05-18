package net.bstjohn.ad.generator.generators

import net.bstjohn.ad.generator.format.common.Meta
import net.bstjohn.ad.generator.generators.CommonGenerators.generateString

import scala.util.Random

object MetaGenerator {
  def generateMeta() = {
    Meta(
      methods = Random.nextInt(),
      `type` = generateString(),
      count = Random.nextInt(),
      version = Random.nextInt()

    )
  }
}
