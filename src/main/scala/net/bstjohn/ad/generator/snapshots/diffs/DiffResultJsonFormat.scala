package net.bstjohn.ad.generator.snapshots.diffs

import com.softwaremill.diffx.DiffResult
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.generic.auto._

object DiffResultJsonFormat {

  implicit val DiffResultEncoder: Encoder[DiffResult] = Encoder.encodeString.contramap(diff => diff.show())


}
