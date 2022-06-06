package net.bstjohn.ad.generator.format.computers

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json, JsonObject}
import net.bstjohn.ad.generator.format.ace.Ace

case class Computer(
  PrimaryGroupSID: Option[String],
  AllowedToDelegate: Seq[String],
  AllowedToAct: Seq[String],
  HasSIDHistory: Seq[Json],
  Sessions: JsonObject,
  PrivilegedSessions: JsonObject,
  RegistrySessions: JsonObject,
  LocalAdmins: JsonObject,
  RemoteDesktopUsers: JsonObject,
  DcomUsers: JsonObject,
  PSRemoteUsers: JsonObject,
  Status: Option[Json],
  Aces: Seq[Ace],
  ObjectIdentifier: String,
  IsDeleted: Boolean,
  IsACLProtected: Boolean,
  Properties: ComputerProperties,
) 

object Computer {
  implicit val ComputerDecoder: Decoder[Computer] = deriveDecoder[Computer]
  implicit val ComputerEncoder: Encoder[Computer] = deriveEncoder[Computer]


}