package net.bstjohn.ad.generator.format.computers

import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class Sessions(
  Results: Option[Seq[Session]],
  Collected: Option[Boolean],
  FailureReason: Option[Json]
) {
  def sessions: Seq[Session] = Results.getOrElse(Seq.empty)

  def withSessions(sessions: Seq[Session]): Sessions =
    copy(Results = Some(sessions))

  def addSession(session: Session): Sessions = Results match {
    case Some(sessions) =>
      copy(Results = Some(sessions :+ session))
    case None =>
      copy(Results = Some(Seq(session)))
  }
}

object Sessions {
  implicit val SessionsDecoder: Decoder[Sessions] = deriveDecoder[Sessions]
  implicit val SessionsEncoder: Encoder[Sessions] = deriveEncoder[Sessions]

}
