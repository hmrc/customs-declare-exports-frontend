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

package forms.declaration.additionaldocuments

import forms.declaration.additionaldocuments.DocumentWriteOff.{documentQuantityPointer, measurementUnitPointer}
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import play.api.data.{Form, FormError, Forms}
import play.api.data.Forms.{optional, text}
import play.api.libs.json.Json
import services.DiffTools
import services.DiffTools.{combinePointers, compareBigDecimalDifference, compareStringDifference, ExportsDeclarationDiff}
import utils.validators.forms.FieldValidator._

case class DocumentWriteOff(measurementUnit: Option[String], documentQuantity: Option[BigDecimal]) extends DiffTools[DocumentWriteOff] {
  def createDiff(original: DocumentWriteOff, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(
      compareStringDifference(original.measurementUnit, measurementUnit, combinePointers(pointerString, measurementUnitPointer, sequenceId)),
      compareBigDecimalDifference(original.documentQuantity, documentQuantity, combinePointers(pointerString, documentQuantityPointer, sequenceId))
    ).flatten

  def measurementUnitDisplay: String = measurementUnit.map(_.replace("#", " ")).getOrElse("")
}

object DocumentWriteOff extends FieldMapping {

  val pointer: ExportsFieldPointer = "documentWriteOff"
  val measurementUnitPointer: ExportsFieldPointer = "measurementUnit"
  val documentQuantityPointer: ExportsFieldPointer = "documentQuantity"

  def convert(measurementUnit: Option[String], documentQuantity: Option[String]): DocumentWriteOff =
    new DocumentWriteOff(measurementUnit, documentQuantity.map(BigDecimal(_)))

  implicit val format = Json.format[DocumentWriteOff]

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

    def missingUnits =
      if ((writeOff.measurementUnit.isEmpty && writeOff.documentQuantity.isDefined) || writeOff.measurementUnit.exists(_.length == qualifierLength))
        Seq(FormError(s"$documentWriteOffKey.$measurementUnitKey", "declaration.additionalDocument.measurementUnit.error"))
      else Seq.empty

    def missingQuantity =
      if (writeOff.measurementUnit.isDefined && writeOff.documentQuantity.isEmpty)
        Seq(FormError(s"$documentWriteOffKey.$documentQuantityKey", "declaration.additionalDocument.quantity.error"))
      else Seq.empty

    missingUnits ++ missingQuantity
  }
}
