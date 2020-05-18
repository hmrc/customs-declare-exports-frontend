/*
 * Copyright 2020 HM Revenue & Customs
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

package views

import java.time.{ZoneId, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

object ViewDates {

  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
  val submissionDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM uuu 'at' HH:mm").withZone(ZoneId.of("Europe/London"))
  val dateAtTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM uuu 'at' HH:mm").withZone(ZoneId.of("Europe/London"))
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM uuu").withZone(ZoneId.of("Europe/London"))

  def format(temporal: TemporalAccessor): String = formatter.format(temporal)
  def formatDateTimeFullMonth(temporal: TemporalAccessor): String = submissionDateTimeFormatter.format(temporal)
  def formatDateAtTime(temporal: TemporalAccessor): String = dateAtTimeFormatter.format(temporal)
  def formatDate(temporal: TemporalAccessor): String = dateFormatter.format(temporal)

}
