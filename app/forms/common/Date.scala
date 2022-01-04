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

package forms.common

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import play.api.data.Forms.{number, optional}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json

import scala.util.Try

case class Date(day: Option[Int], month: Option[Int], year: Option[Int]) {

  private val formatDisplay = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  def toDisplayFormat: String = LocalDate.parse(this.toString).format(formatDisplay)

  override def toString: String = LocalDate.of(year.getOrElse(0), month.getOrElse(0), day.getOrElse(0)).toString
}

object Date {
  implicit val format = Json.format[Date]

  val yearKey = "year"
  val monthKey = "month"
  val dayKey = "day"

  private val dateLowerLimit = LocalDate.of(1999, 12, 31)
  private val dateUpperLimit = LocalDate.of(2100, 1, 1)

  private val isDateFormatValid: Date => Boolean =
    date => Try(LocalDate.parse(date.toString)).isSuccess

  private val isDateInRange: Date => Boolean = date =>
    LocalDate.parse(date.toString).isAfter(dateLowerLimit) && LocalDate.parse(date.toString).isBefore(dateUpperLimit)

  def mapping(formatError: String = "dateTime.date.error.format", rangeError: String = "dateTime.date.error.outOfRange") =
    Forms
      .mapping(
        dayKey -> optional(number()).verifying("dateTime.date.day.error.empty", _.nonEmpty),
        monthKey -> optional(number()).verifying("dateTime.date.month.error.empty", _.nonEmpty),
        yearKey -> optional(number()).verifying("dateTime.date.year.error.empty", _.nonEmpty)
      )(Date.apply)(Date.unapply)
      .verifying(formatError, isDateFormatValid)
      .verifying(rangeError, date => !isDateFormatValid(date) || isDateInRange(date))

  def form(): Form[Date] = Form(mapping())
}
