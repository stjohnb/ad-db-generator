package net.bstjohn.ad.generator.generators.entities

import net.bstjohn.ad.generator.format.ace.{Ace, AcePrincipalType, RightName}
import net.bstjohn.ad.generator.generators.common.CommonGenerators.{genBoolean, genString}

import scala.util.Random

object AceGenerator {
  def generateAce(): Ace = {
    Ace(
      PrincipalSID = genString(),
      PrincipalType = Random.shuffle(AcePrincipalType.values).head,
      RightName = Random.shuffle(RightName.values).head,
      genBoolean()
    )
  }

  def generateAces(): Set[Ace] = {
    (0 to Random.nextInt(10)).map(_ => generateAce()).toSet
  }
}
