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

package forms

import play.api.data.Forms.text
import play.api.libs.json.{Format, JsString, Reads, Writes}
import utils.validators.forms.FieldValidator._

case class Mrn(value: String)

object Mrn {
  implicit val format: Format[Mrn] =
    Format[Mrn](Reads.StringReads.map(Mrn.apply), Writes[Mrn](mrn => JsString(mrn.value)))

  def validRegex: String = "\\d{2}[a-zA-Z]{2}[a-zA-Z0-9]{14}"

  val isValid: String => Boolean = (input: String) => input.matches(validRegex)

  def mapping(prefix: String) =
    text()
      .verifying(s"$prefix.error.empty", nonEmpty)
      .verifying(s"$prefix.error.invalid", isEmpty or isValid)
      .transform[Mrn](Mrn.apply, _.value)
}
