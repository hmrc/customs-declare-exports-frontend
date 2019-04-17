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
import java.time.format.DateTimeFormatter

import play.api.data.Forms.{number, optional}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.isInRange

import scala.util.Try

case class Date(year: Option[Int], month: Option[Int], day: Option[Int]) {

  private val format102 = DateTimeFormatter.ofPattern("yyyyMMdd")

  def to102Format: String = LocalDate.parse(this.toString).format(format102)

  override def toString: String = LocalDate.of(year.getOrElse(0), month.getOrElse(0), day.getOrElse(0)).toString
}

object Date {
  implicit val format = Json.format[Date]

  val yearKey = "year"
  val monthKey = "month"
  val dayKey = "day"

  private val yearLowerLimit = 2000
  private val yearUpperLimit = 2099

  private val isDateFormatValid: Date => Boolean =
    date => Try(LocalDate.parse(date.toString)).isSuccess

  val mapping = Forms
    .mapping(
      yearKey -> optional(
        number().verifying("dateTime.date.year.error.outOfRange", isInRange(yearLowerLimit, yearUpperLimit))
      ).verifying("dateTime.date.year.error.empty", _.nonEmpty),
      monthKey -> optional(number()).verifying("dateTime.date.month.error.empty", _.nonEmpty),
      dayKey -> optional(number()).verifying("dateTime.date.day.error.empty", _.nonEmpty)
    )(Date.apply)(Date.unapply)
    .verifying("dateTime.date.error", isDateFormatValid)

  def form(): Form[Date] = Form(mapping)
}
