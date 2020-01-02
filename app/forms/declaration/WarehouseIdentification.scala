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

package forms.declaration

import forms.DeclarationPage
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class WarehouseIdentification(identificationNumber: Option[String] = None)

object WarehouseIdentification extends DeclarationPage {
  implicit val format = Json.format[WarehouseIdentification]

  val formId = "WarehouseIdentification"

  val validWarehouseTypes = Set('R', 'S', 'T', 'U', 'Y', 'Z')

  val mapping = Forms
    .mapping(
      "identificationNumber" ->
        optional(
          text().verifying(
            "declaration.warehouse.identification.identificationNumber.error",
            startsWith(validWarehouseTypes) and noShorterThan(2) and noLongerThan(36) and isAlphanumeric
          )
        )
    )(WarehouseIdentification.apply)(WarehouseIdentification.unapply)

  def form(): Form[WarehouseIdentification] = Form(mapping)
}
