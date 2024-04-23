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

package services.cache

import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.No
import forms.declaration.{FiscalInformation, _}
import forms.declaration.additionaldocuments.AdditionalDocument
import models.declaration.ProcedureCodesData.osrProcedureCode
import models.declaration.{CommodityMeasure => CommodityMeasureModel, _}

import java.util.UUID

// scalastyle:off
trait ExportsItemBuilder {

  val fiscalInformation: FiscalInformation = FiscalInformation("Yes")
  val fiscalReference: AdditionalFiscalReference = AdditionalFiscalReference("PL", "REFERENCE")
  val listOfFiscalReferences: Seq[AdditionalFiscalReference] = List(fiscalReference)
  val fiscalReferences: AdditionalFiscalReferencesData = AdditionalFiscalReferencesData(listOfFiscalReferences)

  private def uuid: String = UUID.randomUUID.toString

  private val modelWithDefaults: ExportItem = ExportItem(id = uuid)

  private type ItemModifier = ExportItem => ExportItem

  def anItem(modifiers: ItemModifier*): ExportItem =
    modifiers.foldLeft(modelWithDefaults)((current, modifier) => modifier(current))

  def anItemAfter(item: ExportItem, modifiers: ItemModifier*): ExportItem =
    modifiers.foldLeft(item)((current, modifier) => modifier(current))

  // ************************************************* Builders ********************************************************

  def withItemId(id: String): ItemModifier = _.copy(id = id)

  def withSequenceId(id: Int): ItemModifier = _.copy(sequenceId = id)

  def withoutProcedureCodes(): ItemModifier = _.copy(procedureCodes = None)

  def withProcedureCodes(procedureCode: Option[String] = Some(osrProcedureCode), additionalProcedureCodes: Seq[String] = Seq.empty): ItemModifier =
    _.copy(procedureCodes = Some(ProcedureCodesData(procedureCode, additionalProcedureCodes)))

  def withCommodityMeasure(commodityMeasure: CommodityMeasureModel): ItemModifier =
    _.copy(commodityMeasure = Some(commodityMeasure))

  def withFiscalInformation(fiscalInformation: FiscalInformation = fiscalInformation): ItemModifier =
    _.copy(fiscalInformation = Some(fiscalInformation))

  def withAdditionalFiscalReferenceData(data: AdditionalFiscalReferencesData = fiscalReferences): ItemModifier =
    _.copy(additionalFiscalReferencesData = Some(data))

  def withCommodityDetails(data: CommodityDetails): ItemModifier =
    _.copy(commodityDetails = Some(data))

  def withUNDangerousGoodsCode(data: UNDangerousGoodsCode): ItemModifier =
    _.copy(dangerousGoodsCode = Some(data))

  def withCUSCode(data: CusCode): ItemModifier =
    _.copy(cusCode = Some(data))

  def withNactCodes(first: NactCode, others: NactCode*): ItemModifier =
    withNactCodes(List(first) ++ others.toList)

  def withNactCodes(codes: List[NactCode]): ItemModifier =
    _.copy(nactCodes = Some(codes))

  def withNactExemptionCode(code: NactCode): ItemModifier =
    _.copy(nactExemptionCode = Some(code))

  def withStatisticalValue(statisticalValue: String = ""): ItemModifier =
    withStatisticalValue(StatisticalValue(statisticalValue))

  def withStatisticalValue(data: StatisticalValue): ItemModifier = _.copy(statisticalValue = Some(data))

  def withoutPackageInformation(): ItemModifier = _.copy(packageInformation = None)

  def withPackageInformation(first: PackageInformation, others: PackageInformation*): ItemModifier =
    withPackageInformation(List(first) ++ others.toList)

  def withPackageInformation(informations: List[PackageInformation]): ItemModifier =
    _.copy(packageInformation = Some(informations))

  def withPackageInformation(typesOfPackages: String = "", numberOfPackages: Int = 0, shippingMarks: String = ""): ItemModifier =
    exportItem => {
      val packageInfos = exportItem.packageInformation.getOrElse(List.empty)
      val packageInformation = PackageInformation(
        id = UUID.randomUUID().toString,
        typesOfPackages = Some(typesOfPackages),
        numberOfPackages = Some(numberOfPackages),
        shippingMarks = Some(shippingMarks)
      )

      exportItem.copy(packageInformation = Some(packageInfos :+ packageInformation))
    }

  def withAdditionalInformation(code: String, description: String): ItemModifier =
    withAdditionalInformation(AdditionalInformation(code, description))

  def withAdditionalInformation(info1: AdditionalInformation, other: AdditionalInformation*): ItemModifier =
    cache => {
      val existing: Seq[AdditionalInformation] = cache.additionalInformation.map(_.items).getOrElse(Seq.empty)
      cache.copy(additionalInformation = Some(AdditionalInformationData(existing ++ Seq(info1) ++ other)))
    }

  def withAdditionalInformationData(informationData: AdditionalInformationData): ItemModifier =
    cache => cache.copy(additionalInformation = Some(informationData))

  def withoutAdditionalInformation: ItemModifier = _.copy(additionalInformation = None)

  def withoutAdditionalInformation(withIsRequired: Boolean = false): ItemModifier =
    _.copy(additionalInformation = if (withIsRequired) Some(AdditionalInformationData(No, Seq.empty)) else None)

  def withIsLicenseRequired(isRequired: Boolean = true): ItemModifier = cache => cache.copy(isLicenceRequired = Some(isRequired))

  def withNoLicense: ItemModifier = cache => cache.copy(isLicenceRequired = None)

  def withAdditionalDocuments(isRequired: Option[YesNoAnswer], first: AdditionalDocument, documents: AdditionalDocument*): ItemModifier =
    cache => {
      val existing = cache.additionalDocuments.map(_.documents).getOrElse(Seq.empty)
      cache.copy(additionalDocuments = Some(AdditionalDocuments(isRequired, existing ++ Seq(first) ++ documents)))
    }

  def withAdditionalDocuments(additionalDocuments: AdditionalDocuments): ItemModifier =
    cache => cache.copy(additionalDocuments = Some(additionalDocuments))

  def withoutAdditionalDocuments(withIsRequired: Boolean = false): ItemModifier =
    _.copy(additionalDocuments = if (withIsRequired) Some(AdditionalDocuments(No, Seq.empty)) else None)

  def withAdditionalDocument(typeCode: String, identifier: String): AdditionalDocument =
    AdditionalDocument(Some(typeCode), Some(identifier), None, None, None, None, None)

  def withAdditionalDocument(maybeTypeCode: Option[String], maybeIdentifier: Option[String]): AdditionalDocument =
    AdditionalDocument(maybeTypeCode, maybeIdentifier, None, None, None, None, None)
}
