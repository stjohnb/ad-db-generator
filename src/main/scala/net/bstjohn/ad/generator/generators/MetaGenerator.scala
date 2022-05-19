package net.bstjohn.ad.generator.generators

import net.bstjohn.ad.generator.format.common.Meta
import net.bstjohn.ad.generator.generators.CommonGenerators.genString

import scala.util.Random

object MetaGenerator {
  def generateMeta() = {
    Meta(
      methods = Random.nextInt(),
      `type` = genString(),
      count = Random.nextInt(),
      version = Random.nextInt()

    )
  }
}
