package net.bstjohn.ad.generator.format.groups

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.bstjohn.ad.generator.format.ace.Ace
import net.bstjohn.ad.generator.format.users.User

case class Group(
  Members: Seq[GroupMember],
  Aces: Seq[Ace],
  ObjectIdentifier: String,
  IsDeleted: Boolean,
  IsACLProtected: Boolean,
  Properties: GroupProperties,
) {

  def withGroupMember(user: User): Group =
    copy(
      Members = Members.toList :+ GroupMember.fromUser(user))

  def withGroupMember(group: Group): Group =
    copy(
      Members = Members.toList :+ GroupMember.fromGroup(group))


  def withGroupMembers(users: Seq[User]): Group = {
    copy(
      Members = Members.toList ++ users.map(GroupMember.fromUser)
    )
  }

  def withAces(aces: Ace*): Group = {
    copy(Aces = this.Aces ++ aces)
  }
}

object Group {
  implicit val GroupDecoder: Decoder[Group] = deriveDecoder[Group]
  implicit val GroupEncoder: Encoder[Group] = deriveEncoder[Group]


}