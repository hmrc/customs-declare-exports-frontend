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
import forms.common.YesNoAnswer.valueForYesNo
import forms.declaration.FiscalInformation.AllowedFiscalInformationAnswers.yes
import forms.declaration._
import models.AmendmentRow.{forAddedValue, forRemovedValue}
import models.DeclarationMeta.sequenceIdPlaceholder
import models.DeclarationType.DeclarationType
import models.ExportsFieldPointer.ExportsFieldPointer
import models.declaration.ExportItem.keyForIsLicenceRequired
import models.viewmodels.TariffContentKey
import models.{AmendmentOp, DeclarationType, FieldMapping}
import play.api.i18n.Messages
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
      compareDifference(original.taricCodes, taricCodes, combinePointers(pointerString, TaricCode.pointer, maybeSequenceId)),
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

  def hasFiscalReferences: Boolean =
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
      if (hasFiscalReferences) additionalFiscalReferencesData.isDefined
      else fiscalInformation.isDefined

    procedureCodes.flatMap(_.procedureCode).isDefined &&
    (procedureCodes.flatMap(_.procedureCode).exists(!ProcedureCodesData.osrProcedureCodes.contains(_)) || isFiscalInformationCompleted)
  }

  def requiresWarehouseId: Boolean =
    procedureCodes.flatMap(_.procedureCode).exists(ProcedureCodesData.isWarehouseRequiredCode)

  def valueAdded(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    procedureCodes.fold("")(_.valueAdded(s"$pointer.${ProcedureCodesData.pointer}")) +
      additionalFiscalReferencesData.fold("")(_.references.zipWithIndex.map { case (reference, index) =>
        reference.valueAdded(s"$pointer.${AdditionalFiscalReferencesData.pointer}.${index + 1}")
      }.mkString) +
      statisticalValue.fold("")(_.valueAdded(s"$pointer.${StatisticalValue.pointer}")) +
      commodityDetails.fold("")(_.valueAdded(s"$pointer.${CommodityDetails.pointer}")) +
      dangerousGoodsCode.fold("")(_.valueAdded(s"$pointer.${UNDangerousGoodsCode.pointer}")) +
      cusCode.fold("")(_.valueAdded(s"$pointer.${CusCode.pointer}")) +
      taricCodes.fold("")(_.zipWithIndex.map { case (taricCode, index) =>
        taricCode.valueAdded(s"$pointer.${TaricCode.pointer}.${index + 1}")
      }.mkString) +
      nactCodes.fold("")(_.zipWithIndex.map { case (nactCode, index) =>
        nactCode.valueAdded(s"$pointer.${NactCode.pointer}.${index + 1}")
      }.mkString) +
      nactExemptionCode.fold("")(_.valueAdded(s"$pointer.${NactCode.exemptionPointer}")) +
      packageInformation.fold("")(_.zipWithIndex.map { case (packageInfo, index) =>
        packageInfo.valueAdded(s"$pointer.${PackageInformation.pointer}.${index + 1}")
      }.mkString) +
      commodityMeasure.fold("")(_.valueAdded(s"$pointer.${CommodityMeasure.pointer}")) +
      additionalInformation.fold("")(_.items.zipWithIndex.map { case (additionalInfo, index) =>
        additionalInfo.valueAdded(s"$pointer.${AdditionalInformationData.pointer}.${index + 1}")
      }.mkString) +
      additionalDocuments.fold("")(_.documents.zipWithIndex.map { case (document, index) =>
        document.valueAdded(s"$pointer.${AdditionalDocuments.pointer}.${index + 1}")
      }.mkString) +
      isLicenceRequired.fold("")(ilr => forAddedValue(s"$pointer.licences", messages(keyForIsLicenceRequired), valueForYesNo(ilr)))

  def valueRemoved(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    procedureCodes.fold("")(_.valueRemoved(s"$pointer.${ProcedureCodesData.pointer}")) +
      additionalFiscalReferencesData.fold("")(_.references.zipWithIndex.map { case (reference, index) =>
        reference.valueRemoved(s"$pointer.${AdditionalFiscalReferencesData.pointer}.${index + 1}")
      }.mkString) +
      statisticalValue.fold("")(_.valueRemoved(s"$pointer.${StatisticalValue.pointer}")) +
      commodityDetails.fold("")(_.valueRemoved(s"$pointer.${CommodityDetails.pointer}")) +
      dangerousGoodsCode.fold("")(_.valueRemoved(s"$pointer.${UNDangerousGoodsCode.pointer}")) +
      cusCode.fold("")(_.valueRemoved(s"$pointer.${CusCode.pointer}")) +
      taricCodes.fold("")(_.zipWithIndex.map { case (taricCode, index) =>
        taricCode.valueRemoved(s"$pointer.${TaricCode.pointer}.${index + 1}")
      }.mkString) +
      nactCodes.fold("")(_.zipWithIndex.map { case (nactCode, index) =>
        nactCode.valueRemoved(s"$pointer.${NactCode.pointer}.${index + 1}")
      }.mkString) +
      nactExemptionCode.fold("")(_.valueRemoved(s"$pointer.${NactCode.exemptionPointer}")) +
      packageInformation.fold("")(_.zipWithIndex.map { case (packageInfo, index) =>
        packageInfo.valueRemoved(s"$pointer.${PackageInformation.pointer}.${index + 1}")
      }.mkString) +
      commodityMeasure.fold("")(_.valueRemoved(s"$pointer.${CommodityMeasure.pointer}")) +
      additionalInformation.fold("")(_.items.zipWithIndex.map { case (additionalInfo, index) =>
        additionalInfo.valueRemoved(s"$pointer.${AdditionalInformationData.pointer}.${index + 1}")
      }.mkString) +
      additionalDocuments.fold("")(_.documents.zipWithIndex.map { case (document, index) =>
        document.valueRemoved(s"$pointer.${AdditionalDocuments.pointer}.${index + 1}")
      }.mkString) +
      isLicenceRequired.fold("")(ilr => forRemovedValue(s"$pointer.licences", messages(keyForIsLicenceRequired), valueForYesNo(ilr)))
}

object ExportItem extends DeclarationPage with FieldMapping {
  implicit val format: OFormat[ExportItem] = Json.format[ExportItem]

  val pointer: ExportsFieldPointer = "items"
  val sequenceIdPointer: ExportsFieldPointer = "sequenceId"

  // prefix of the message keys used for the 'Amendment details' page
  val itemsPrefix = "declaration.summary.items.item"

  private lazy val keyForIsLicenceRequired = s"${itemsPrefix}.licences"

  def containsAnswers(item: ExportItem): Boolean = item != ExportItem(id = item.id, sequenceId = item.sequenceId)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.declarationItemsList.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
