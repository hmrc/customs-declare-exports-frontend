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

package forms.declaration.additionaldocuments

import play.api.data.Forms.{bigDecimal, optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.{isAlphanumeric, lengthInRange}

case class DocumentWriteOff(measurementUnit: Option[String], documentQuantity: Option[BigDecimal])

object DocumentWriteOff {

  implicit val format = Json.format[DocumentWriteOff]

  private val measurementUnitMinLength = 3
  private val measurementUnitMaxLength = 4
  private val documentQuantityMaxLength = 16
  private val documentQuantityMaxDecimalPlaces = 6

  val measurementUnitKey = "measurementUnit"
  val documentQuantityKey = "documentQuantity"

  val mapping = Forms
    .mapping(
      measurementUnitKey -> optional(
        text()
          .verifying("supplementary.addDocument.measurementUnit.error.length", lengthInRange(measurementUnitMinLength)(measurementUnitMaxLength))
          .verifying("supplementary.addDocument.measurementUnit.error.specialCharacters", isAlphanumeric)
      ),
      documentQuantityKey -> optional(
        bigDecimal
          .verifying("supplementary.addDocument.documentQuantity.error.precision", _.precision <= documentQuantityMaxLength)
          .verifying("supplementary.addDocument.documentQuantity.error.scale", _.scale <= documentQuantityMaxDecimalPlaces)
          .verifying("supplementary.addDocument.documentQuantity.error", _ >= 0)
      )
    )(DocumentWriteOff.apply)(DocumentWriteOff.unapply)
    .verifying("supplementary.addDocument.error.measurementUnitAndQuantity", validateMeasurementUnitAndDocumentQuantity(_))

  private def validateMeasurementUnitAndDocumentQuantity(documentWriteOff: DocumentWriteOff): Boolean =
    (documentWriteOff.measurementUnit.isEmpty && documentWriteOff.documentQuantity.isEmpty) ||
      (documentWriteOff.measurementUnit.nonEmpty && documentWriteOff.documentQuantity.nonEmpty)

  def form(): Form[DocumentWriteOff] = Form(mapping)
}
