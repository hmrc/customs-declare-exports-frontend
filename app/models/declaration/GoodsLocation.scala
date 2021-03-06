/*
 * Copyright 2021 HM Revenue & Customs
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

import forms.declaration.GoodsLocationForm
import play.api.libs.json.{Json, OFormat}

case class GoodsLocation(country: String, typeOfLocation: String, qualifierOfIdentification: String, identificationOfLocation: String) {

  val code = country + typeOfLocation + qualifierOfIdentification + identificationOfLocation

  def toForm(): GoodsLocationForm = GoodsLocationForm(code)
}

object GoodsLocation {

  implicit val format: OFormat[GoodsLocation] = Json.format[GoodsLocation]
}
