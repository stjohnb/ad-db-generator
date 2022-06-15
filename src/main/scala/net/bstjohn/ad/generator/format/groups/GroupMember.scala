package net.bstjohn.ad.generator.format.groups

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.bstjohn.ad.generator.format.users.User

case class GroupMember(
  ObjectIdentifier: String,
  ObjectType: GroupMemberType
)

object GroupMember {

  def fromGroup(group: Group): GroupMember = {
    GroupMember(
      group.ObjectIdentifier.value,
      GroupMemberType.Group
    )
  }

  def fromUser(user: User): GroupMember = {
    GroupMember(
      user.ObjectIdentifier.value,
      GroupMemberType.User
    )
  }

  def fromComputer(): GroupMember = ???

  implicit val GroupMemberDecoder: Decoder[GroupMember] = deriveDecoder[GroupMember]
  implicit val GroupMemberEncoder: Encoder[GroupMember] = deriveEncoder[GroupMember]

}
