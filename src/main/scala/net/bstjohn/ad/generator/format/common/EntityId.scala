package net.bstjohn.ad.generator.format.common

import io.circe.{Decoder, Encoder}

sealed trait EntityId {
  def value: String
}

object EntityId {

  case class GroupId(value: String) extends EntityId
  case class UserId(value: String) extends EntityId

  object GroupId {
    implicit val GroupIdDecoder: Decoder[GroupId] = Decoder.decodeString.map(value => GroupId(value))
    implicit val GroupIdEncoder: Encoder[GroupId] = Encoder.encodeString.contramap(_.value)
  }

  object UserId {
    implicit val UserIdDecoder: Decoder[UserId] = Decoder.decodeString.map(value => UserId(value))
    implicit val UserIdEncoder: Encoder[UserId] = Encoder.encodeString.contramap(_.value)
  }


}

