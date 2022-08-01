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
}

object Sessions {
  implicit val SessionsDecoder: Decoder[Sessions] = deriveDecoder[Sessions]
  implicit val SessionsEncoder: Encoder[Sessions] = deriveEncoder[Sessions]

}
