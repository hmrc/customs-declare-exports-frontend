/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.declaration._
import forms.declaration.additionaldocuments.AdditionalDocument
import models.declaration.{CommodityMeasure => CommodityMeasureModel, _}

import java.util.UUID

trait ExportsItemBuilder {

  private def uuid: String = UUID.randomUUID.toString

  private val modelWithDefaults: ExportItem = ExportItem(id = uuid)

  private type ItemModifier = ExportItem => ExportItem

  def anItem(modifiers: ItemModifier*): ExportItem =
    modifiers.foldLeft(modelWithDefaults)((current, modifier) => modifier(current))

  // ************************************************* Builders ********************************************************

  def withItemId(id: String): ItemModifier = _.copy(id = id)

  def withSequenceId(id: Int): ItemModifier = _.copy(sequenceId = id)

  def withoutProcedureCodes(): ItemModifier = _.copy(procedureCodes = None)

  def withProcedureCodes(procedureCode: Option[String] = Some("1042"), additionalProcedureCodes: Seq[String] = Seq.empty): ItemModifier =
    _.copy(procedureCodes = Some(ProcedureCodesData(procedureCode, additionalProcedureCodes)))

  def withoutAdditionalInformation(): ItemModifier = _.copy(additionalInformation = None)

  def withCommodityMeasure(commodityMeasure: CommodityMeasureModel): ItemModifier =
    _.copy(commodityMeasure = Some(commodityMeasure))

  def withFiscalInformation(fiscalInformation: FiscalInformation): ItemModifier =
    _.copy(fiscalInformation = Some(fiscalInformation))

  def withAdditionalFiscalReferenceData(data: AdditionalFiscalReferencesData): ItemModifier =
    _.copy(additionalFiscalReferencesData = Some(data))

  def withAdditionalInformation(code: String, description: String): ItemModifier =
    withAdditionalInformation(AdditionalInformation(code, description))

  def withAdditionalInformation(info1: AdditionalInformation, other: AdditionalInformation*): ItemModifier =
    cache => {
      val existing: Seq[AdditionalInformation] = cache.additionalInformation.map(_.items).getOrElse(Seq.empty)
      cache.copy(additionalInformation = Some(AdditionalInformationData(existing ++ Seq(info1) ++ other)))
    }

  def withAdditionalInformationData(informationData: AdditionalInformationData): ItemModifier =
    cache => cache.copy(additionalInformation = Some(informationData))

  def withCommodityDetails(data: CommodityDetails): ItemModifier =
    _.copy(commodityDetails = Some(data))

  def withUNDangerousGoodsCode(data: UNDangerousGoodsCode): ItemModifier =
    _.copy(dangerousGoodsCode = Some(data))

  def withCUSCode(data: CusCode): ItemModifier =
    _.copy(cusCode = Some(data))

  def withTaricCodes(first: TaricCode, others: TaricCode*): ItemModifier =
    withTaricCodes(List(first) ++ others.toList)

  def withTaricCodes(codes: List[TaricCode]): ItemModifier =
    _.copy(taricCodes = Some(codes))

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
    cache =>
      cache.copy(packageInformation =
        Some(
          cache.packageInformation.getOrElse(List.empty) :+ PackageInformation(
            UUID.randomUUID().toString,
            Some(typesOfPackages),
            Some(numberOfPackages),
            Some(shippingMarks)
          )
        )
      )

  def withAdditionalDocuments(isRequired: Option[YesNoAnswer], first: AdditionalDocument, documents: AdditionalDocument*): ItemModifier =
    cache => {
      val existing = cache.additionalDocuments.map(_.documents).getOrElse(Seq.empty)
      cache.copy(additionalDocuments = Some(AdditionalDocuments(isRequired, existing ++ Seq(first) ++ documents)))
    }

  def withAdditionalDocuments(additionalDocuments: AdditionalDocuments): ItemModifier =
    cache => cache.copy(additionalDocuments = Some(additionalDocuments))

  def withLicenseRequired(): ItemModifier =
    cache => cache.copy(isLicenceRequired = Some(true))

  def withLicenseNotRequired(): ItemModifier =
    cache => cache.copy(isLicenceRequired = Some(false))
}
