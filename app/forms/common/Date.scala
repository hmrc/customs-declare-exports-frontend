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

import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.{isContainedIn, isNumeric}

case class Date(year: Option[String], month: Option[String], day: Option[String]) {

  def isEmpty: Boolean = year.isEmpty && month.isEmpty && day.isEmpty
  def nonEmpty: Boolean = year.nonEmpty && month.nonEmpty && day.nonEmpty
}

object Date {
  implicit val format = Json.format[Date]

  private val days = (1 to 31).toList.map(_.toString)
  private val months = (1 to 12).toList.map(_.toString)

  val mapping = Forms.mapping(
    "year" -> optional(
      text()
        .verifying("dateTime.date.year.error", year => isNumeric(year) && (year.toInt >= 2000 && year.toInt < 2100))
    ),
    "month" -> optional(text().verifying("dateTime.date.month.error", isContainedIn(months))),
    "day" -> optional(text().verifying("dateTime.date.day.error", isContainedIn(days)))
  )(Date.apply)(Date.unapply).verifying("dateTime.date.error", validateDate(_))

  private def validateDate(date: Date): Boolean = date.isEmpty || date.nonEmpty

  def form(): Form[Date] = Form(mapping)
}
