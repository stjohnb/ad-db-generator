package net.bstjohn.ad.generator.snapshots.diffs

import com.softwaremill.diffx.DiffResult
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.generic.auto._

object DiffResultJsonFormat {

//  implicit val DiffResultEncoder: Encoder[DiffResult] = Encoder.encodeString.contramap(diff => diff.show())
//  implicit val DiffResultEncoder: Encoder[DiffResult] = deriveEncoder[DiffResult]

//  implicit val DiffResultEncoder: Encoder[DiffResult] = Encoder.instance {
//    case foo @ Foo(_) => foo.asJson
//    case bar @ Bar(_) => bar.asJson
//    case baz @ Baz(_) => baz.asJson
//    case qux @ Qux(_) => qux.asJson
//  }

}
