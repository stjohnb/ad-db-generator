package net.bstjohn.ad.generator.generators.common

import io.circe.JsonObject
import net.bstjohn.ad.generator.format.common.EntityId.{GroupId, UserId}

import java.util.UUID
import scala.util.Random

object CommonGenerators {
  def genSid(): String = genString()

  def genGroupId(): GroupId = GroupId(genSid())

  def genUserId(): UserId = UserId(genSid())

  def genString(): String = UUID.randomUUID().toString

  def genInt(): Int = Random.nextInt()

  def genLong(): Long = Random.nextLong()

  def genBoolean(): Boolean = Random.nextBoolean()

  def genOption(): Option[Unit] = if (genBoolean()) Some(()) else None

  def genJsonObject(): JsonObject = JsonObject()


}
