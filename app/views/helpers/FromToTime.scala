/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package views.helpers

import play.api.i18n.Messages

import java.time._
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.Locale

case class FromToTime(fromHour: String, fromDate: String, toHour: String, toDate: String)

object FromToTime {

  private val HOUR_PATTERN = "h:mma"
  private val DATE_PATTERN = "EEEE d MMMM uuuu"
  private val DAY_MONTH_YEAR_PATTERN = "d MMMM uuuu"

  private val zoneId = ZoneId.of("Europe/London")

  def apply(fromDateTimeString: String, toDateTimeString: String)(implicit messages: Messages): FromToTime = {
    val locale = messages.lang.toLocale
    val (fromHour, fromDate) = parseAndFormat(fromDateTimeString, locale)
    val (toHour, toDate) = parseAndFormat(toDateTimeString, locale)

    FromToTime(fromHour, fromDate, toHour, toDate)
  }

  def formatDate(temporal: TemporalAccessor)(implicit messages: Messages): String =
    format(temporal, DAY_MONTH_YEAR_PATTERN)

  def formatDateAtTime(temporal: TemporalAccessor)(implicit messages: Messages): String =
    s"${formatDate(temporal)} ${messages("dateTime.at")} ${format(temporal, HOUR_PATTERN).toLowerCase()}"

  private def format(temporal: TemporalAccessor, pattern: String)(implicit messages: Messages): String =
    DateTimeFormatter.ofPattern(pattern, messages.lang.toLocale).withZone(zoneId).format(temporal)

  private def formatDateTime(dateTime: ZonedDateTime, pattern: String, locale: Locale): String =
    dateTime.format(DateTimeFormatter.ofPattern(pattern, locale))

  private def parseAndFormat(dateTimeString: String, locale: Locale): (String, String) = {
    val dateTime = ZonedDateTime.parse(dateTimeString)
    val hour = formatDateTime(dateTime, HOUR_PATTERN, locale).toLowerCase()
    val date = formatDateTime(dateTime, DATE_PATTERN, locale)
    (hour, date)
  }
}
