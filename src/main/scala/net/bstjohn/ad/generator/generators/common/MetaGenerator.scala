package net.bstjohn.ad.generator.generators.common

import net.bstjohn.ad.generator.format.common.{Meta, MetaType}

import scala.util.Random

object MetaGenerator {
  def generateMeta(
    `type`: MetaType = Random.shuffle(MetaType.values).head,
    count: Int = Random.nextInt()
  ) = {
    Meta(
      methods = 29675,
      `type` = `type`,
      count = count,
      version = 4
    )
  }
}
