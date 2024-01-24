/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.declaration.countries

import forms.declaration.countries.Country.mappingsForAmendment
import models.AmendmentRow.{forAddedValue, forAmendedValue, forRemovedValue}
import models.ExportsFieldPointer.ExportsFieldPointer
import models.declaration.Locations.destinationCountryPointer
import models.{Amendment, FieldMapping}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}

case class Country(code: Option[String]) extends Ordered[Country] with Amendment {

  override def compare(that: Country): Int =
    (code, that.code) match {
      case (None, None)                    => 0
      case (_, None)                       => 1
      case (None, _)                       => -1
      case (Some(current), Some(original)) => current.compare(original)
    }

  def value: String = code.getOrElse("")

  def valueAdded(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    code.fold("")(forAddedValue(pointer, messages(mappingsForAmendment(pointer)), _))

  def valueAmended(newValue: Amendment, pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    forAmendedValue(pointer, messages(mappingsForAmendment(pointer)), value, newValue.value)

  def valueRemoved(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    code.fold("")(forRemovedValue(pointer, messages(mappingsForAmendment(pointer)), _))
}

object Country extends FieldMapping {

  val pointer: ExportsFieldPointer = "code"

  def mappingsForAmendment(pointer: ExportsFieldPointer): String =
    if (pointer.endsWith(destinationCountryPointer)) "declaration.summary.countries.countryOfDestination"
    else "declaration.summary.countries.routingCountry"

  implicit val format: OFormat[Country] = Json.format[Country]

  val GB = Country(Some("GB"))
}
