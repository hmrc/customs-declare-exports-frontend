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

package forms.declaration
import forms.DeclarationPage
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class TaricCode(taricCode: String)

object TaricCode extends DeclarationPage {

  implicit val format = Json.format[TaricCode]

  val taricCodeKey = "taricCode"
  val taricCodeLength = 4
  val taricCodeLimit = 99

  val mapping =
    Forms.mapping(
      taricCodeKey ->
        text()
          .verifying("declaration.taricAdditionalCodes.error.empty", nonEmpty)
          .verifying("declaration.taricAdditionalCodes.error.invalid", isEmpty or (hasSpecificLength(taricCodeLength) and isAlphanumeric))
    )(TaricCode.apply)(TaricCode.unapply)

  def form(): Form[TaricCode] = Form(mapping)
}
