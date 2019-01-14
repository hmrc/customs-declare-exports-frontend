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

package forms

import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.data.validation.Constraints.pattern
import play.api.libs.json.Json

case class Ducr(ducr: String)

object Ducr {
  implicit val format = Json.format[Ducr]

  private val ducrFormat = "^\\d[A-Z]{2}\\d{12}-[0-9A-Z]{1,19}$"

  val ducrMapping =
    mapping("ducr" -> text().verifying(pattern(ducrFormat.r, error = "error.ducr")))(Ducr.apply)(Ducr.unapply)

  val id = "DUCR"

  def form(): Form[Ducr] = Form(ducrMapping)
}
