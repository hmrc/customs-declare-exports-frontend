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

import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.isContainedIn

case class Time(hour: Option[String], minute: Option[String]) {

  def isEmpty: Boolean = hour.isEmpty && minute.isEmpty
  def nonEmpty: Boolean = hour.nonEmpty && minute.nonEmpty
}

object Time {
  implicit val format = Json.format[Time]

  private val validHours = (0 to 23).toList.map(_.toString)
  private val validMinutes = (0 to 59).toList.map(_.toString)

  val mapping = Forms
    .mapping(
      "hour" -> optional(text().verifying("dateTime.time.hour.error", isContainedIn(validHours))),
      "minute" -> optional(text().verifying("dateTime.time.minute.error", isContainedIn(validMinutes)))
    )(Time.apply)(Time.unapply)
    .verifying("dateTime.time.error", validateTime(_))

  private def validateTime(time: Time): Boolean = time.nonEmpty || time.isEmpty

  def form(): Form[Time] = Form(mapping)
}
