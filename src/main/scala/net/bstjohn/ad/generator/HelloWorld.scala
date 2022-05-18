package net.bstjohn.ad.generator

import cats.effect.IO
import net.bstjohn.ad.generator.reader.ZipSnapshotReader

object HelloWorld {

  def say(): IO[Unit] = for {
    snap1 <- ZipSnapshotReader.read("/Users/brendanstjohn/queens/ad-db-snapshots/20220506180850_BloodHound.zip")
    snap2 <- ZipSnapshotReader.read("/Users/brendanstjohn/queens/ad-db-snapshots/20220507091000_BloodHound.zip")
    snap3 <- ZipSnapshotReader.read("/Users/brendanstjohn/queens/ad-db-snapshots/20220509120734_BloodHound.zip")
  } yield {
    snap1.zip(snap2).zip(snap3).map { case ((s1, s2), s3) =>
      println(s"Snapshot 1: ${s1.users.data.size} users, ${s1.groups.data.size} groups")
      println(s"Snapshot 2: ${s2.users.data.size} users, ${s2.groups.data.size} groups")
      println(s"Snapshot 3: ${s3.users.data.size} users, ${s3.groups.data.size} groups")
    }
  }
}
