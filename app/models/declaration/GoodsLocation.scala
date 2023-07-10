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

package models.declaration

import forms.declaration.LocationOfGoods
import models.AmendmentRow.{forAddedValue, forAmendedValue, forRemovedValue}
import models.ExportsFieldPointer.ExportsFieldPointer
import models.declaration.GoodsLocation.keyForAmend
import models.{Amendment, FieldMapping}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import services.DiffTools.{combinePointers, ExportsDeclarationDiff}
import services.{AlteredField, DiffTools, OriginalAndNewValues}

case class GoodsLocation(country: String, typeOfLocation: String, qualifierOfIdentification: String, identificationOfLocation: String)
    extends DiffTools[GoodsLocation] with Amendment {

  def createDiff(original: GoodsLocation, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    // special implementation to ensure GoodsLocation entity returned as value diff instead of individual field values
    Seq(
      Option.when(
        !country.compare(original.country).equals(0) ||
          !typeOfLocation.compare(original.typeOfLocation).equals(0) ||
          !qualifierOfIdentification.compare(original.qualifierOfIdentification).equals(0) ||
          !identificationOfLocation.compare(original.identificationOfLocation).equals(0)
      )(AlteredField(combinePointers(pointerString, sequenceId), OriginalAndNewValues(Some(original), Some(this))))
    ).flatten

  def value: String = country + typeOfLocation + qualifierOfIdentification + identificationOfLocation

  def valueAdded(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    forAddedValue(pointer, messages(keyForAmend), value)

  def valueAmended(newValue: Amendment, pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    forAmendedValue(pointer, messages(keyForAmend), value, newValue.value)

  def valueRemoved(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    forRemovedValue(pointer, messages(keyForAmend), value)

  def toForm: LocationOfGoods = LocationOfGoods(value)
}

object GoodsLocation extends FieldMapping {

  val pointer: ExportsFieldPointer = "goodsLocation"

  val keyForAmend = "declaration.summary.locations.goodsLocationCode"

  implicit val format: OFormat[GoodsLocation] = Json.format[GoodsLocation]
}
