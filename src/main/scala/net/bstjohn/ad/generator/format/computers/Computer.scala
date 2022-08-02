package net.bstjohn.ad.generator.format.computers

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json, JsonObject}
import net.bstjohn.ad.generator.format.ace.{Ace, RightName}
import net.bstjohn.ad.generator.format.groups.Group
import net.bstjohn.ad.generator.format.users.User

case class Computer(
  PrimaryGroupSID: Option[String],
  AllowedToDelegate: Seq[String],
  AllowedToAct: Seq[String],
  HasSIDHistory: Seq[Json],
  Sessions: Sessions,
  PrivilegedSessions: Sessions,
  RegistrySessions: Sessions,
  LocalAdmins: LocalAdmins,
  RemoteDesktopUsers: JsonObject,
  DcomUsers: JsonObject,
  PSRemoteUsers: JsonObject,
  Status: Option[Json],
  Aces: Seq[Ace],
  ObjectIdentifier: String,
  IsDeleted: Boolean,
  IsACLProtected: Boolean,
  Properties: ComputerProperties,
) {
  def withAce(ace: Ace): Computer = copy(Aces = Aces :+ ace)

  def withSessions(users: Seq[User]): Computer = {
    val sessions = users.map(u => Session(u.ObjectIdentifier, ObjectIdentifier))

    copy(Sessions = Sessions.withSessions(sessions))
  }

  def withAces(aces: Seq[Ace]): Computer = copy(Aces = Aces ++ aces)

  def ownedBy(group: Group): Computer =
    withAce(Ace(group.ObjectIdentifier, RightName.GenericAll))

  def allSessions: Seq[Session] = Sessions.sessions ++ PrivilegedSessions.sessions ++ RegistrySessions.sessions

  def localAdmins: Seq[LocalAdmin] = LocalAdmins.localAdmins
}

object Computer {
  implicit val ComputerDecoder: Decoder[Computer] = deriveDecoder[Computer]
  implicit val ComputerEncoder: Encoder[Computer] = deriveEncoder[Computer]


}