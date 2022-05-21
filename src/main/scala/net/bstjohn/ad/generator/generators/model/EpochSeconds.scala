package net.bstjohn.ad.generator.generators.model

import java.text.SimpleDateFormat
import java.util.Date
import scala.util.Random

case class EpochSeconds(
  value: Long
) {

  def toDateString: String = {
    val d = new Date(value)
    val f = new SimpleDateFormat("yyyyMMddHHmmss")

    f.format(d)
  }

  def plusRandomSeconds(): EpochSeconds = EpochSeconds(value + Random.nextLong(60))

  def plusSeconds(seconds: Long): EpochSeconds = EpochSeconds(value + seconds).plusRandomSeconds()

  def plusMinutes(minutes: Long): EpochSeconds = this.plusSeconds(minutes * 60)

  def plusHours(hours: Long): EpochSeconds = this.plusSeconds(hours * 60 * 60)

  def plusDays(days: Long): EpochSeconds = this.plusSeconds(days * 24 * 60 * 60)

  def plusMonths(months: Long): EpochSeconds = this.plusSeconds(months * 28 * 24 * 60 * 60)

  def plusYears(years: Long): EpochSeconds = this.plusSeconds(years * 365 * 24 * 60 * 60)
}

object EpochSeconds {
  def fromDate(date: Date) = EpochSeconds(date.getTime)
}