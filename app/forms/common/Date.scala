/*
 * Copyright 2019 HM Revenue & Customs
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

import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.isNumeric

case class Date(year: Option[String], month: Option[String], day: Option[String]) {

  def isEmpty: Boolean = year.isEmpty && month.isEmpty && day.isEmpty
  def nonEmpty: Boolean = year.nonEmpty && month.nonEmpty && day.nonEmpty

  override def toString: String = LocalDate.of(
    year.getOrElse("0000").toInt,
    month.getOrElse("00").toInt,
    day.getOrElse("00").toInt
  ).toString

}

object Date {
  implicit val format = Json.format[Date]

  private val validYears = 2000 to 2099
  private val validMonths = 1 to 12
  private val validDays = 1 to 31

  val yearKey = "year"
  val monthKey = "month"
  val dayKey = "day"

  val mapping = Forms
    .mapping(
      yearKey -> optional(
        text()
          .verifying("dateTime.date.year.error", year => isNumeric(year) && validYears.contains(year.toInt))
      ),
      monthKey -> optional(
        text().verifying("dateTime.date.month.error", month => isNumeric(month) && validMonths.contains(month.toInt))
      ),
      dayKey -> optional(
        text().verifying("dateTime.date.day.error", day => isNumeric(day) && validDays.contains(day.toInt))
      )
    )(Date.apply)(Date.unapply)
    .verifying("dateTime.date.error", validateDate(_))

  private def validateDate(date: Date): Boolean = date.isEmpty || date.nonEmpty

  def form(): Form[Date] = Form(mapping)
}
