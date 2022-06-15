package net.bstjohn.ad.generator.generators.model

import java.text.SimpleDateFormat
import java.util.Date

case class EpochSeconds(
  value: Long
) {

  def toDateString: String = {
    val d = new Date(value)
    val f = new SimpleDateFormat("yyyyMMddHHmmss")

    f.format(d)
  }

  def plusSeconds(seconds: Int): EpochSeconds = EpochSeconds(value + seconds.toLong)

  def plusMinutes(minutes: Int): EpochSeconds = this.plusSeconds(minutes * 60)

  def plusHours(hours: Int): EpochSeconds = this.plusSeconds(hours * 60 * 60)

  def plusDays(days: Int): EpochSeconds = this.plusSeconds(days * 24 * 60 * 60)

  def plusWeeks(weeks: Int): EpochSeconds = this.plusSeconds(weeks * 7 * 24 * 60 * 60)

  def plusMonths(months: Int): EpochSeconds = this.plusSeconds(months * 28 * 24 * 60 * 60)

  def plusYears(years: Int): EpochSeconds = this.plusSeconds(years * 365 * 24 * 60 * 60)
}

object EpochSeconds {
  def fromDate(date: Date) = EpochSeconds(date.getTime)
}