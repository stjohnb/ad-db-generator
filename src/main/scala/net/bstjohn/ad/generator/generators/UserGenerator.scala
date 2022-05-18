package net.bstjohn.ad.generator.generators

import net.bstjohn.ad.generator.format.users.User
import net.bstjohn.ad.generator.generators.CommonGenerators.{generateBoolean, generateJsonObject, generateSid}

object UserGenerator {
  def generateUser(
  ): User = {
    User(
      List.empty,
      None,
      List.empty,
      List.empty,
      List.empty,
      generateSid(),
      generateBoolean(),
      generateBoolean(),
      generateJsonObject()
    )
  }

}
