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

package forms.section5.additionaldocuments

import forms.section5.additionaldocuments.DocumentWriteOff.{
  documentQuantityPointer,
  keyForDocumentQuantity,
  keyForMeasurementUnit,
  measurementUnitPointer
}
import models.AmendmentRow.{forAddedValue, forRemovedValue, pointerToSelector}
import models.ExportsFieldPointer.ExportsFieldPointer
import models.declaration.ExportItem.itemsPrefix
import models.{AmendmentOp, FieldMapping}
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, FormError, Forms}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools.{combinePointers, compareBigDecimalDifference, compareStringDifference, ExportsDeclarationDiff}
import utils.validators.forms.FieldValidator._

case class DocumentWriteOff(measurementUnit: Option[String], documentQuantity: Option[BigDecimal])
    extends DiffTools[DocumentWriteOff] with AmendmentOp {
  def createDiff(original: DocumentWriteOff, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(
      compareStringDifference(original.measurementUnit, measurementUnit, combinePointers(pointerString, measurementUnitPointer, sequenceId)),
      compareBigDecimalDifference(original.documentQuantity, documentQuantity, combinePointers(pointerString, documentQuantityPointer, sequenceId))
    ).flatten

  def measurementUnitDisplay: String = measurementUnit.map(_.replace("#", " ")).getOrElse("")

  def valueAdded(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    measurementUnit.fold("")(forAddedValue(pointerToSelector(pointer, measurementUnitPointer), messages(keyForMeasurementUnit), _)) +
      documentQuantity.fold("")(qty =>
        forAddedValue(pointerToSelector(pointer, documentQuantityPointer), messages(keyForDocumentQuantity), qty.toString)
      )

  def valueRemoved(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    measurementUnit.fold("")(forRemovedValue(pointerToSelector(pointer, measurementUnitPointer), messages(keyForMeasurementUnit), _)) +
      documentQuantity.fold("")(qty =>
        forRemovedValue(pointerToSelector(pointer, documentQuantityPointer), messages(keyForDocumentQuantity), qty.toString)
      )
}

object DocumentWriteOff extends FieldMapping {

  val pointer: ExportsFieldPointer = "documentWriteOff"
  val measurementUnitPointer: ExportsFieldPointer = "measurementUnit"
  val documentQuantityPointer: ExportsFieldPointer = "documentQuantity"

  lazy val keyForMeasurementUnit = s"$itemsPrefix.additionalDocuments.measurementUnit"
  lazy val keyForDocumentQuantity = s"$itemsPrefix.additionalDocuments.measurementUnitQuantity"

  private def convert(measurementUnit: Option[String], documentQuantity: Option[String]): DocumentWriteOff =
    new DocumentWriteOff(measurementUnit, documentQuantity.map(BigDecimal(_)))

  implicit val format: OFormat[DocumentWriteOff] = Json.format[DocumentWriteOff]

  private val measurementUnitLength = 3
  private val qualifierLength = 1
  private val documentQuantityMaxLength = 16
  private val documentQuantityMaxDecimalPlaces = 6

  val documentWriteOffKey = "documentWriteOff"
  val measurementUnitKey = "measurementUnit"
  val qualifierKey = "qualifier"
  val documentQuantityKey = "documentQuantity"

  private def form2Model: (Option[String], Option[String], Option[String]) => DocumentWriteOff = {
    case (unit, None, quantity)      => convert(unit, quantity)
    case (unit, qualifier, quantity) => convert(Some(Seq(unit, qualifier).flatten.mkString("#")), quantity)
  }

  private def model2Form: DocumentWriteOff => Option[(Option[String], Option[String], Option[String])] =
    model => {
      val unitAndQualifier: Option[Array[String]] = model.measurementUnit.map(_.split("#"))
      Some((unitAndQualifier.flatMap(_.headOption), unitAndQualifier.flatMap(_.lift(1)), model.documentQuantity.map(_.toString())))
    }

  val mapping = Forms
    .mapping(
      measurementUnitKey -> optional(
        text()
          .verifying("declaration.additionalDocument.measurementUnit.error", hasSpecificLength(measurementUnitLength) and isAlphanumeric)
      ),
      qualifierKey -> optional(
        text()
          .verifying("declaration.additionalDocument.qualifier.error", hasSpecificLength(qualifierLength) and isAlphanumeric)
      ),
      documentQuantityKey -> optional(
        text()
          .verifying(
            "declaration.additionalDocument.quantity.error",
            input =>
              input.isEmpty || noLongerThan(documentQuantityMaxLength)(input.replaceAll("\\.", ""))
                && isDecimalWithNoMoreDecimalPlacesThan(documentQuantityMaxDecimalPlaces)(input)
          )
      )
    )(form2Model)(model2Form)

  def form: Form[DocumentWriteOff] = Form(mapping)

  def globalErrors(writeOff: DocumentWriteOff): Seq[FormError] = {

    def missingUnits: Seq[FormError] =
      if ((writeOff.measurementUnit.isEmpty && writeOff.documentQuantity.isDefined) || writeOff.measurementUnit.exists(_.length == qualifierLength))
        Seq(FormError(s"$documentWriteOffKey.$measurementUnitKey", "declaration.additionalDocument.measurementUnit.error"))
      else Seq.empty

    def missingQuantity: Seq[FormError] =
      if (writeOff.measurementUnit.isDefined && writeOff.documentQuantity.isEmpty)
        Seq(FormError(s"$documentWriteOffKey.$documentQuantityKey", "declaration.additionalDocument.quantity.error"))
      else Seq.empty

    missingUnits ++ missingQuantity
  }
}
