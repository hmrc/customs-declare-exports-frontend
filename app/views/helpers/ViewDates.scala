/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.Logging
import play.api.i18n.Messages

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField.MONTH_OF_YEAR
import java.time.temporal.TemporalAccessor
import java.time.{Instant, LocalDate, Month, ZoneId}
import scala.util.{Failure, Success, Try}

object ViewDates extends Logging {

  def formatDate(temporal: TemporalAccessor)(implicit messages: Messages): String =
    translateMonthOnWelsh(temporal, dateFormatter.format(temporal))

  def formatDateAtTime(temporal: TemporalAccessor)(implicit messages: Messages): String = {
    val result = translateAmPmOnWelsh(dateTimeFormatter.format(temporal))
    translateMonthOnWelsh(temporal, result)
  }

  def formatTimeDate(temporal: TemporalAccessor)(implicit messages: Messages): String =
    translateMonthOnWelsh(temporal, timeDateFormatter.format(temporal))

  private def translateAmPmOnWelsh(result: String)(implicit messages: Messages): String =
    if (messages.lang.code.toLowerCase != "cy")
      result
        .replace("AM", "am")
        .replace("PM", "pm")
    else
      result
        .replaceFirst("(?i)am", "yb")
        .replaceFirst("(?i)pm", "yh")
        .replace(" at ", " am ")

  private def translateMonthOnWelsh(temporal: TemporalAccessor, result: String)(implicit messages: Messages): String =
    if (messages.lang.code.toLowerCase != "cy") result
    else {
      def localDate: LocalDate = LocalDate.ofInstant(Instant.from(temporal), zoneId)

      Try(Month.from(localDate).get(MONTH_OF_YEAR)) match {
        case Success(month) =>
          monthsForWelsh(month - 1) match {
            case (translateFrom, translateTo) => result.replace(translateFrom, translateTo)
            case _                            => result
          }
        case Failure(exc) =>
          logger.error("While translating the month to Welsh", exc)
          result
      }
    }

  private val zoneId = ZoneId.of("Europe/London")

  private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM uuu").withZone(zoneId)
  private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM uuu 'at' h:mma").withZone(zoneId)
  private val timeDateFormatter = DateTimeFormatter.ofPattern("HH:mm, d MMMM uuu").withZone(zoneId)

  private val monthsForWelsh = Array(
    "January" -> "Ionawr",
    "February" -> "Chwefror",
    "March" -> "Mawrth",
    "April" -> "Ebrill",
    "May" -> "Mai",
    "June" -> "Mehefin",
    "July" -> "Gorffennaf",
    "August" -> "Awst",
    "September" -> "Medi",
    "October" -> "Hydref",
    "November" -> "Tachwedd",
    "December" -> "Rhagfyr"
  )
}
