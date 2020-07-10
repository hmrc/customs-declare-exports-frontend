/*
 * Copyright 2020 HM Revenue & Customs
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

package forms

import play.api.data.Forms.{mapping, text}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.isValidDucr

case class Ducr(ducr: String)

object Ducr {
  implicit val format = Json.format[Ducr]

  def form2Data(ducr: String): Ducr = new Ducr(ducr.toUpperCase)

  val ducrMapping =
    mapping("ducr" -> text().verifying("error.ducr", isValidDucr))(form2Data)(Ducr.unapply)
}
