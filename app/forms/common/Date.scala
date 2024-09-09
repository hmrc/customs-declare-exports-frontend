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

package forms.common

import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import play.api.data.{Form, Forms}
import play.api.data.Forms.{number, optional}
import play.api.libs.json.{Json, OFormat}
import services.{AlteredField, DiffTools, OriginalAndNewValues}
import services.DiffTools.{compareOptInt, ExportsDeclarationDiff}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

case class Date(day: Option[Int], month: Option[Int], year: Option[Int]) extends DiffTools[Date] {

  // Special implementation to treat any single field difference as a difference for the whole date
  def createDiff(original: Date, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff = {
    val differences = Seq(compareOptInt(original.day, day), compareOptInt(original.month, month), compareOptInt(original.year, year))

    if (differences != Seq(0, 0, 0))
      Seq(AlteredField(pointerString, OriginalAndNewValues(Some(original), Some(this))))
    else
      Seq()
  }

  private val formatDisplay = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  def toDisplayFormat: String = LocalDate.parse(this.toString).format(formatDisplay)

  override def toString: String = LocalDate.of(year.getOrElse(1), month.getOrElse(1), day.getOrElse(1)).toString
}

object Date extends FieldMapping {
  implicit val format: OFormat[Date] = Json.format[Date]

  val pointer: ExportsFieldPointer = "dateOfValidity"
  val dayPointer: ExportsFieldPointer = "day"
  val monthPointer: ExportsFieldPointer = "month"
  val yearPointer: ExportsFieldPointer = "year"

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

  def form: Form[Date] = Form(mapping())
}
