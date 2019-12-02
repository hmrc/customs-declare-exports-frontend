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
import forms.declaration.TransportCodes.allowedModeOfTransportCodes
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.isContainedIn

case class InlandModeOfTransportCode(inlandModeOfTransportCode: Option[String] = None)

object InlandModeOfTransportCode extends DeclarationPage {
  implicit val format = Json.format[InlandModeOfTransportCode]

  val formId = "InlandModeOfTransportCode"

  val mapping = Forms
    .mapping(
      "inlandModeOfTransportCode" -> optional(
        text()
          .verifying("declaration.warehouse.inlandTransportDetails.error.incorrect", isContainedIn(allowedModeOfTransportCodes))
      )
    )(InlandModeOfTransportCode.apply)(InlandModeOfTransportCode.unapply)

  def form(): Form[InlandModeOfTransportCode] = Form(mapping)
}
