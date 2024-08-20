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

package models.declaration

import forms.DeclarationPage
import forms.section5.FiscalInformation.AllowedFiscalInformationAnswers.yes
import forms.section5._
import models.DeclarationMeta.sequenceIdPlaceholder
import models.DeclarationType.DeclarationType
import models.ExportsFieldPointer.ExportsFieldPointer
import models.viewmodels.TariffContentKey
import models.{AmendmentOp, DeclarationType, FieldMapping}
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools._

case class ExportItem(
  id: String,
  sequenceId: Int = sequenceIdPlaceholder,
  procedureCodes: Option[ProcedureCodesData] = None,
  fiscalInformation: Option[FiscalInformation] = None,
  additionalFiscalReferencesData: Option[AdditionalFiscalReferencesData] = None,
  statisticalValue: Option[StatisticalValue] = None,
  commodityDetails: Option[CommodityDetails] = None,
  dangerousGoodsCode: Option[UNDangerousGoodsCode] = None,
  cusCode: Option[CusCode] = None,
  taricCodes: Option[List[TaricCode]] = None,
  nactCodes: Option[List[NactCode]] = None,
  nactExemptionCode: Option[NactCode] = None,
  packageInformation: Option[List[PackageInformation]] = None,
  commodityMeasure: Option[CommodityMeasure] = None,
  additionalInformation: Option[AdditionalInformationData] = None,
  additionalDocuments: Option[AdditionalDocuments] = None,
  isLicenceRequired: Option[Boolean] = None
) extends DiffTools[ExportItem] with ExplicitlySequencedObject[ExportItem] with AmendmentOp {

  val totalPackages: Int = packageInformation.map(_.flatMap(_.numberOfPackages).sum).getOrElse(0)

  // id and fiscalInformation fields are not used to create WCO XML
  override def createDiff(original: ExportItem, pointerString: ExportsFieldPointer, maybeSequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(
      compareIntDifference(original.sequenceId, sequenceId, combinePointers(pointerString, ExportItem.sequenceIdPointer, maybeSequenceId)),
      createDiffOfOptions(original.procedureCodes, procedureCodes, combinePointers(pointerString, ProcedureCodesData.pointer, maybeSequenceId)),
      createDiffOfOptionIsos(
        original.additionalFiscalReferencesData,
        additionalFiscalReferencesData,
        combinePointers(pointerString, AdditionalFiscalReferencesData.pointer, maybeSequenceId)
      ),
      compareDifference(original.statisticalValue, statisticalValue, combinePointers(pointerString, StatisticalValue.pointer, maybeSequenceId)),
      createDiffOfOptions(original.commodityDetails, commodityDetails, combinePointers(pointerString, CommodityDetails.pointer, maybeSequenceId)),
      compareDifference(
        original.dangerousGoodsCode,
        dangerousGoodsCode,
        combinePointers(pointerString, UNDangerousGoodsCode.pointer, maybeSequenceId)
      ),
      compareDifference(original.cusCode, cusCode, combinePointers(pointerString, CusCode.pointer, maybeSequenceId)),
      compareDifference(original.nactCodes, nactCodes, combinePointers(pointerString, NactCode.pointer, maybeSequenceId)),
      compareDifference(original.nactExemptionCode, nactExemptionCode, combinePointers(pointerString, NactCode.exemptionPointer, maybeSequenceId)),
      createDiff(original.packageInformation, packageInformation, combinePointers(pointerString, PackageInformation.pointer, maybeSequenceId)),
      createDiffOfOptions(original.commodityMeasure, commodityMeasure, combinePointers(pointerString, CommodityMeasure.pointer, maybeSequenceId)),
      createDiffOfOptionIsos(
        original.additionalInformation,
        additionalInformation,
        combinePointers(pointerString, AdditionalInformationData.pointer, maybeSequenceId)
      ),
      createDiffOfOptionIsos(
        original.additionalDocuments,
        additionalDocuments,
        combinePointers(pointerString, AdditionalDocuments.pointer, maybeSequenceId)
      )
    ).flatten

  override def updateSequenceId(sequenceId: Int): ExportItem = copy(sequenceId = sequenceId)

  lazy val isDefined: Boolean =
    procedureCodes.isDefined ||
      fiscalInformation.isDefined ||
      additionalFiscalReferencesData.isDefined ||
      statisticalValue.isDefined ||
      commodityDetails.isDefined ||
      dangerousGoodsCode.isDefined ||
      cusCode.isDefined ||
      nactCodes.isDefined ||
      nactExemptionCode.isDefined ||
      packageInformation.isDefined ||
      commodityMeasure.isDefined ||
      additionalInformation.isDefined ||
      additionalDocuments.isDefined ||
      isLicenceRequired.isDefined

  def hasFiscalInformation: Boolean =
    fiscalInformation.exists(_.onwardSupplyRelief == yes)

  val isCompleted: PartialFunction[DeclarationType, Boolean] = {
    case DeclarationType.STANDARD | DeclarationType.SUPPLEMENTARY =>
      isProcedureCodesAndFiscalInformationComplete && statisticalValue.isDefined &&
        packageInformation.nonEmpty && commodityMeasure.isDefined

    case DeclarationType.SIMPLIFIED | DeclarationType.OCCASIONAL =>
      isProcedureCodesAndFiscalInformationComplete && packageInformation.nonEmpty

    case DeclarationType.CLEARANCE =>
      isProcedureCodesAndFiscalInformationComplete &&
        isProcedureCodesAndExportInventoryCleansingRecordComplete
  }

  private def isProcedureCodesAndExportInventoryCleansingRecordComplete =
    if (isExportInventoryCleansingRecord) packageInformation.isEmpty else packageInformation.nonEmpty

  def isExportInventoryCleansingRecord: Boolean =
    procedureCodes
      .flatMap(_.procedureCode)
      .exists(code => ProcedureCodesData.eicrProcedureCodes.contains(code))

  private def isProcedureCodesAndFiscalInformationComplete = {

    def isFiscalInformationCompleted: Boolean =
      if (hasFiscalInformation) additionalFiscalReferencesData.isDefined
      else fiscalInformation.isDefined

    procedureCodes.flatMap(_.procedureCode).isDefined &&
    (!procedureCodes.exists(_.hasOsrProcedureCode) || isFiscalInformationCompleted)
  }

  def requiresWarehouseId: Boolean =
    procedureCodes.flatMap(_.procedureCode).exists(ProcedureCodesData.isWarehouseRequiredCode)

  def getLeafPointersIfAny(pointer: ExportsFieldPointer): Seq[ExportsFieldPointer] =
    Seq(pointer)
}

object ExportItem extends DeclarationPage with FieldMapping {
  implicit val format: OFormat[ExportItem] = Json.format[ExportItem]

  val pointer: ExportsFieldPointer = "items"
  val sequenceIdPointer: ExportsFieldPointer = "sequenceId"

  // prefix of the message keys used for the 'Amendment details' page
  val itemsPrefix = "declaration.summary.item"

  def containsAnswers(item: ExportItem): Boolean = item != ExportItem(id = item.id, sequenceId = item.sequenceId)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.declarationItemsList.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
