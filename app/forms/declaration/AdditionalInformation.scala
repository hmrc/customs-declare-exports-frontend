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

package forms.declaration

import forms.DeclarationPage
import play.api.data.Forms._
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class AdditionalInformation(code: String, description: String) {
  override def toString: String = s"${code}-${description}"
}

object AdditionalInformation extends DeclarationPage {
  implicit val format = Json.format[AdditionalInformation]

  val formId = "AdditionalInformation"

  val mapping = Forms.mapping(
    "code" ->
      text()
        .verifying("supplementary.additionalInformation.code.empty", nonEmpty)
        .verifying("supplementary.additionalInformation.code.error", isEmpty or (isAlphanumeric and hasSpecificLength(5))),
    "description" ->
      text()
        .verifying(
          "supplementary.additionalInformation.description.error",
          isEmpty or (noLongerThan(70) and isAlphanumericWithAllowedSpecialCharacters)
        )
  )(AdditionalInformation.apply)(AdditionalInformation.unapply)

  def form(): Form[AdditionalInformation] = Form(mapping)
}
