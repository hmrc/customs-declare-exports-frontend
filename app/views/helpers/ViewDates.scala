/*
 * Copyright 2022 HM Revenue & Customs
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

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

object ViewDates {

  def formatDate(temporal: TemporalAccessor): String = dateFormatter.format(temporal)

  def formatDateAtTime(temporal: TemporalAccessor): String =
    dateTimeFormatter
      .format(temporal)
      .replace("AM", "am")
      .replace("PM", "pm")

  def formatTimeDate(temporal: TemporalAccessor): String = timeDateFormatter.format(temporal)

  private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMMM uuu").withZone(ZoneId.of("Europe/London"))

  private val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMMM uuu 'at' h:mma").withZone(ZoneId.of("Europe/London"))

  private val timeDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm, d MMMM uuu").withZone(ZoneId.of("Europe/London"))
}
