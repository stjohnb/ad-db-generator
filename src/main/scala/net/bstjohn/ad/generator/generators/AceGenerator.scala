package net.bstjohn.ad.generator.generators

import net.bstjohn.ad.generator.format.common.{Ace, AcePrincipalType}
import net.bstjohn.ad.generator.generators.CommonGenerators.{genBoolean, genString}

import scala.util.Random

object AceGenerator {
  def generateAce(): Ace = {
    Ace(
      PrincipalSID = genString(),
      PrincipalType = Random.shuffle(AcePrincipalType.values).head,
      genString(),
      genBoolean()
    )
  }

  def generateAces(): Set[Ace] = {
    (0 to Random.nextInt(10)).map(_ => generateAce()).toSet
  }
}
