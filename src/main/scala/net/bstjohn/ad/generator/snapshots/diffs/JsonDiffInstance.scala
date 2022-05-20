package net.bstjohn.ad.generator.snapshots.diffs

import com.softwaremill.diffx.{Diff, DiffContext, DiffResult, FieldPath}
import io.circe.Json

object JsonDiffInstance {
  implicit def diffForJson(difForString: Diff[String]): Diff[Json] =
    (left: Json, right: Json, context: DiffContext) => {
      difForString.apply(left.noSpaces, right.noSpaces)
    }

  implicit def diffForOptionJson(difForString: Diff[String]): Diff[Option[Json]] =
    (left: Option[Json], right: Option[Json], context: DiffContext) => {
      difForString.apply(
        left.map(_.noSpaces).getOrElse("none"),
        right.map(_.noSpaces).getOrElse("none"))
    }

  implicit def diffForOptionListJson(difForString: Diff[String]): Diff[Option[List[Json]]] =
    (left: Option[List[Json]], right: Option[List[Json]], context: DiffContext) => {
      difForString.apply(
        left.map(_.map(_.noSpaces).toString()).getOrElse("none"),
        right.map(_.map(_.noSpaces).toString()).getOrElse("none"))
    }
}
