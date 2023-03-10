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
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools.{combinePointers, compareStringDifference, ExportsDeclarationDiff}

case class GoodsLocation(country: String, typeOfLocation: String, qualifierOfIdentification: String, identificationOfLocation: String)
    extends DiffTools[GoodsLocation] {
  def createDiff(original: GoodsLocation, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(
      compareStringDifference(original.country, country, combinePointers(pointerString, GoodsLocation.countryPointer, sequenceId)),
      compareStringDifference(
        original.typeOfLocation,
        typeOfLocation,
        combinePointers(pointerString, GoodsLocation.typeOfLocationPointer, sequenceId)
      ),
      compareStringDifference(
        original.qualifierOfIdentification,
        qualifierOfIdentification,
        combinePointers(pointerString, GoodsLocation.qualifierOfIdentificationPointer, sequenceId)
      ),
      compareStringDifference(
        original.identificationOfLocation,
        identificationOfLocation,
        combinePointers(pointerString, GoodsLocation.identificationOfLocationPointer, sequenceId)
      )
    ).flatten

  lazy val code = country + typeOfLocation + qualifierOfIdentification + identificationOfLocation

  def toForm: LocationOfGoods = LocationOfGoods(code)
}

object GoodsLocation extends FieldMapping {
  implicit val format: OFormat[GoodsLocation] = Json.format[GoodsLocation]

  val pointer: ExportsFieldPointer = "goodsLocation"
  val countryPointer: ExportsFieldPointer = "country"
  val typeOfLocationPointer: ExportsFieldPointer = "typeOfLocation"
  val qualifierOfIdentificationPointer: ExportsFieldPointer = "qualifierOfIdentification"
  val identificationOfLocationPointer: ExportsFieldPointer = "identificationOfLocation"
}
