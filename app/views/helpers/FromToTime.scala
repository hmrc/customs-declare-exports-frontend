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

import models.requests.VerifiedEmailRequest
import play.api.i18n.I18nSupport.RequestWithMessagesApi
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContent

import java.time._
import java.time.format.DateTimeFormatter
import java.util.Locale

case class FromToTime(fromHour: String, fromDate: String, toHour: String, toDate: String)

object FromToTime {

  private val HOUR_PATTERN = "ha"
  private val DATE_PATTERN = "EEEE d MMMM YYYY"

  def apply(fromDateTimeString: String, toDateTimeString: String)(implicit request: VerifiedEmailRequest[AnyContent], messagesApi: MessagesApi): FromToTime = {
    val locale = request.lang.toLocale
    val (fromHour, fromDate) = parseAndFormat(fromDateTimeString, locale)
    val (toHour, toDate) = parseAndFormat(toDateTimeString, locale)

    FromToTime(fromHour, fromDate, toHour, toDate)
  }

  private def formatDateTime(
                      dateTime: ZonedDateTime,
                      pattern: String,
                      locale: Locale
                    ): String =
    dateTime.format(DateTimeFormatter.ofPattern(pattern, locale))

  private def parseAndFormat(dateTimeString: String, locale: Locale): (String, String) = {
    val dateTime = ZonedDateTime.parse(dateTimeString)
    val hour = formatDateTime(dateTime, HOUR_PATTERN, locale).toLowerCase()
    val date = formatDateTime(dateTime, DATE_PATTERN, locale)
    (hour, date)
  }
}