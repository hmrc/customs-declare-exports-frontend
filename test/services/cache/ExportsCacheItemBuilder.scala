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

import forms.declaration.additionaldocuments.DocumentsProduced
import forms.declaration.{AdditionalInformation, ItemType, PackageInformation}
import models.declaration.{AdditionalInformationData, DocumentsProducedData, ProcedureCodesData}

trait ExportsCacheItemBuilder {

  private def uuid: String = UUID.randomUUID().toString

  private val modelWithDefaults: ExportItem = ExportItem(id = uuid)

  private type CachedItemModifier = ExportItem => ExportItem

  def aCachedItem(modifiers: (CachedItemModifier)*): ExportItem =
    modifiers.foldLeft(modelWithDefaults)((current, modifier) => modifier(current))

  // ************************************************* Builders ********************************************************

  def withProcedureCodes(
    procedureCode: Option[String] = None,
    additionalProcedureCodes: Seq[String] = Seq.empty
  ): CachedItemModifier =
    _.copy(procedureCodes = Some(ProcedureCodesData(procedureCode, additionalProcedureCodes)))

  def withAdditionalInformation(code: String, description: String): CachedItemModifier =
    withAdditionalInformation(AdditionalInformation(code, description))

  def withAdditionalInformation(data: AdditionalInformation*): CachedItemModifier =
    cache => {
      val existing: Seq[AdditionalInformation] = cache.additionalInformation.map(_.items).getOrElse(Seq.empty)
      cache.copy(additionalInformation = Some(AdditionalInformationData(existing ++ data)))
    }

  def withoutItemType(): CachedItemModifier = _.copy(itemType = None)

  def withItemType(
    combinedNomenclatureCode: String = "",
    taricAdditionalCodes: Seq[String] = Seq.empty,
    nationalAdditionalCodes: Seq[String] = Seq.empty,
    descriptionOfGoods: String = "",
    cusCode: Option[String] = None,
    unDangerousGoodsCode: Option[String] = None,
    statisticalValue: String = ""
  ): CachedItemModifier =
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

  def withItemType(data: ItemType): CachedItemModifier = _.copy(itemType = Some(data))

  def withPackageInformation(info: PackageInformation*): CachedItemModifier = _.copy(packageInformation = info.toList)

  def withPackageInformation(typesOfPackages: Option[String],
                             numberOfPackages: Option[Int],
                             shippingMarks: Option[String]
                            ): CachedItemModifier = cache => cache.copy(packageInformation = cache.packageInformation :+ PackageInformation(
    typesOfPackages,
    numberOfPackages,
    shippingMarks)
  )

  def withDocumentsProduced(docs: DocumentsProduced*): CachedItemModifier = cache => {
    val existing = cache.documentsProducedData.map(_.documents).getOrElse(Seq.empty)
    cache.copy(documentsProducedData = Some(DocumentsProducedData(existing ++ docs)))
  }

  def withDocumentsProduced(docs: DocumentsProducedData): CachedItemModifier = cache => cache.copy(documentsProducedData = Some(docs))

}
