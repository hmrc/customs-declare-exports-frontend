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

package services.cache

import java.util.UUID

import forms.declaration._
import forms.declaration.additionaldocuments.DocumentsProduced
import models.declaration._

trait ExportsItemBuilder {

  private def uuid: String = UUID.randomUUID().toString

  private val modelWithDefaults: ExportItem = ExportItem(id = uuid)

  private type ItemModifier = ExportItem => ExportItem

  def anItem(modifiers: (ItemModifier)*): ExportItem =
    modifiers.foldLeft(modelWithDefaults)((current, modifier) => modifier(current))

  // ************************************************* Builders ********************************************************

  def withItemId(id: String): ItemModifier = _.copy(id = id)

  def withSequenceId(id: Int): ItemModifier = _.copy(sequenceId = id)

  def withoutProcedureCodes(): ItemModifier = _.copy(procedureCodes = None)

  def withProcedureCodes(procedureCode: Option[String] = None, additionalProcedureCodes: Seq[String] = Seq.empty): ItemModifier =
    _.copy(procedureCodes = Some(ProcedureCodesData(procedureCode, additionalProcedureCodes)))

  def withoutAdditionalInformation(): ItemModifier = _.copy(additionalInformation = None)

  def withCommodityMeasure(commodityMeasure: CommodityMeasure): ItemModifier =
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

  def withoutItemType(): ItemModifier = _.copy(itemType = None)

  def withItemType(
    combinedNomenclatureCode: String = "",
    taricAdditionalCodes: Seq[String] = Seq.empty,
    nationalAdditionalCodes: Seq[String] = Seq.empty,
    descriptionOfGoods: String = "",
    cusCode: Option[String] = None,
    unDangerousGoodsCode: Option[String] = None,
    statisticalValue: String = ""
  ): ItemModifier =
    withItemType(
      ItemType(
        combinedNomenclatureCode,
        taricAdditionalCodes,
        nationalAdditionalCodes,
        descriptionOfGoods,
        cusCode,
        unDangerousGoodsCode,
        statisticalValue
      )
    )

  def withItemType(data: ItemType): ItemModifier = _.copy(itemType = Some(data))

  def withoutPackageInformation(): ItemModifier = _.copy(packageInformation = List.empty)

  def withPackageInformation(first: PackageInformation, others: PackageInformation*): ItemModifier =
    withPackageInformation(List(first) ++ others.toList)

  def withPackageInformation(informations: List[PackageInformation]): ItemModifier =
    _.copy(packageInformation = informations)

  def withPackageInformation(typesOfPackages: String = "", numberOfPackages: Int = 0, shippingMarks: String = ""): ItemModifier =
    cache => cache.copy(packageInformation = cache.packageInformation :+ PackageInformation(typesOfPackages, numberOfPackages, shippingMarks))

  def withDocumentsProduced(first: DocumentsProduced, docs: DocumentsProduced*): ItemModifier = cache => {
    val existing = cache.documentsProducedData.map(_.documents).getOrElse(Seq.empty)
    cache.copy(documentsProducedData = Some(DocumentsProducedData(existing ++ Seq(first) ++ docs)))
  }

  def withDocumentsProducedData(docs: DocumentsProducedData): ItemModifier =
    cache => cache.copy(documentsProducedData = Some(docs))

}
