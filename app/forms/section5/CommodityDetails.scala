/*
 * Copyright 2024 HM Revenue & Customs
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

package forms.section5

import forms.DeclarationPage
import forms.section1.AdditionalDeclarationType.SUPPLEMENTARY_EIDR
import forms.section5.CommodityDetails.{combinedNomenclatureCodePointer, descriptionOfGoodsPointer}
import models.DeclarationType.*
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import models.declaration.ExportItem.itemsPrefix
import models.requests.JourneyRequest
import models.viewmodels.TariffContentKey
import play.api.data.Forms.{mapping, optional, text}
import play.api.data.{Form, Mapping}
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools.{combinePointers, compareStringDifference, ExportsDeclarationDiff}
import utils.validators.forms.FieldValidator.*

case class CommodityDetails(combinedNomenclatureCode: Option[String], descriptionOfGoods: Option[String]) extends DiffTools[CommodityDetails] {

  def createDiff(original: CommodityDetails, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    List(
      compareStringDifference(
        original.combinedNomenclatureCode,
        combinedNomenclatureCode,
        combinePointers(pointerString, combinedNomenclatureCodePointer, sequenceId)
      ),
      compareStringDifference(original.descriptionOfGoods, descriptionOfGoods, combinePointers(pointerString, descriptionOfGoodsPointer, sequenceId))
    ).flatten
}

object CommodityDetails extends DeclarationPage with FieldMapping {

  implicit val format: OFormat[CommodityDetails] = Json.format[CommodityDetails]

  val pointer: ExportsFieldPointer = "commodityDetails"
  val combinedNomenclatureCodePointer: ExportsFieldPointer = "combinedNomenclatureCode"
  val descriptionOfGoodsPointer: ExportsFieldPointer = "descriptionOfGoods"

  lazy val keyForCode = s"$itemsPrefix.commodityCode"
  lazy val keyForDescription = s"$itemsPrefix.goodsDescription"

  val placeholder = "NNNNNNNNNN"

  val combinedNomenclatureCodeKey = "combinedNomenclatureCode"
  val descriptionOfGoodsKey = "descriptionOfGoods"
  val descriptionOfGoodsMaxLength = 280
  val commodityCodeChemicalPrefixes = List(28, 29, 38)

  private def mappingCombinedNomenclatureCodeRequired: Mapping[Option[String]] =
    mappingCombinedNomenclatureCodeOptional
      .verifying("declaration.commodityDetails.combinedNomenclatureCode.error.empty", isSome)

  private def mappingCombinedNomenclatureCodeOptional: Mapping[Option[String]] =
    optional(
      text()
        .transform(_.trim, identity[String])
        .verifying("declaration.commodityDetails.combinedNomenclatureCode.error.empty", nonEmpty)
        .verifying("declaration.commodityDetails.combinedNomenclatureCode.error.invalid", isEmpty or (isNumeric and hasSpecificLength(8)))
    )

  private val mappingDescriptionOfGoodsRequired =
    optional(
      text()
        .verifying("declaration.commodityDetails.description.error.empty", nonEmpty)
        .verifying("declaration.commodityDetails.description.error.length", isEmpty or noLongerThan(descriptionOfGoodsMaxLength))
    ).verifying("declaration.commodityDetails.description.error.empty", isSome)

  private val mappingDescriptionOfGoodsOptional =
    optional(
      text()
        .verifying("declaration.commodityDetails.description.error.empty", nonEmpty)
        .verifying("declaration.commodityDetails.description.error.length", isEmpty or noLongerThan(descriptionOfGoodsMaxLength))
    )

  private val mappingRequiredCode: Mapping[CommodityDetails] =
    mapping(combinedNomenclatureCodeKey -> mappingCombinedNomenclatureCodeRequired, descriptionOfGoodsKey -> mappingDescriptionOfGoodsRequired)(
      CommodityDetails.apply
    )(CommodityDetails => Some(Tuple.fromProductTyped(CommodityDetails)))

  private val mappingOptionalCode: Mapping[CommodityDetails] =
    mapping(combinedNomenclatureCodeKey -> mappingCombinedNomenclatureCodeOptional, descriptionOfGoodsKey -> mappingDescriptionOfGoodsRequired)(
      CommodityDetails.apply
    )(CommodityDetails => Some(Tuple.fromProductTyped(CommodityDetails)))

  private val mappingOptionalCodeAndOptionalDescription: Mapping[CommodityDetails] =
    mapping(combinedNomenclatureCodeKey -> mappingCombinedNomenclatureCodeOptional, descriptionOfGoodsKey -> mappingDescriptionOfGoodsOptional)(
      CommodityDetails.apply
    )(CommodityDetails => Some(Tuple.fromProductTyped(CommodityDetails)))

  def form(implicit request: JourneyRequest[_]): Form[CommodityDetails] = {
    val isEidr =
      request.isAdditionalDeclarationType(SUPPLEMENTARY_EIDR) ||
        request.isType(CLEARANCE) && request.cacheModel.isEntryIntoDeclarantsRecords

    request.declarationType match {
      case CLEARANCE | SUPPLEMENTARY if isEidr => Form(mappingOptionalCode)
      case CLEARANCE                           => Form(mappingOptionalCodeAndOptionalDescription)
      case SIMPLIFIED | OCCASIONAL             => Form(mappingOptionalCode)
      case _                                   => Form(mappingRequiredCode)
    }
  }

  override def defineTariffContentKeys(declarationType: DeclarationType): Seq[TariffContentKey] =
    declarationType match {
      case CLEARANCE =>
        List(TariffContentKey("tariff.declaration.item.commodityDetails.clearance"))
      case _ =>
        List(
          TariffContentKey("tariff.declaration.item.commodityDetails.1.common"),
          TariffContentKey("tariff.declaration.item.commodityDetails.2.common"),
          TariffContentKey("tariff.declaration.item.commodityDetails.3.common")
        )
    }
}
