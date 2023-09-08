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

package forms.declaration

import forms.DeclarationPage
import forms.declaration.CommodityDetails.{combinedNomenclatureCodePointer, descriptionOfGoodsPointer, keyForCode, keyForDescription}
import models.AmendmentRow.{forAddedValue, forRemovedValue, pointerToSelector}
import models.{AmendmentOp, DeclarationType, FieldMapping}
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.viewmodels.TariffContentKey
import models.ExportsFieldPointer.ExportsFieldPointer
import models.declaration.ExportItem.itemsPrefix
import play.api.data.{Form, Mapping}
import play.api.data.Forms.{mapping, optional, text}
import play.api.i18n.Messages
import play.api.libs.json.Json
import services.DiffTools
import services.DiffTools.{combinePointers, compareStringDifference, ExportsDeclarationDiff}
import utils.validators.forms.FieldValidator._

case class CommodityDetails(combinedNomenclatureCode: Option[String], descriptionOfGoods: Option[String])
    extends DiffTools[CommodityDetails] with AmendmentOp {

  def createDiff(original: CommodityDetails, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(
      compareStringDifference(
        original.combinedNomenclatureCode,
        combinedNomenclatureCode,
        combinePointers(pointerString, combinedNomenclatureCodePointer, sequenceId)
      ),
      compareStringDifference(original.descriptionOfGoods, descriptionOfGoods, combinePointers(pointerString, descriptionOfGoodsPointer, sequenceId))
    ).flatten

  def valueAdded(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    combinedNomenclatureCode.fold("")(forAddedValue(pointerToSelector(pointer, combinedNomenclatureCodePointer), messages(keyForCode), _)) +
      descriptionOfGoods.fold("")(forAddedValue(pointerToSelector(pointer, descriptionOfGoodsPointer), messages(keyForDescription), _))

  def valueRemoved(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    combinedNomenclatureCode.fold("")(forRemovedValue(pointerToSelector(pointer, combinedNomenclatureCodePointer), messages(keyForCode), _)) +
      descriptionOfGoods.fold("")(forRemovedValue(pointerToSelector(pointer, descriptionOfGoodsPointer), messages(keyForDescription), _))
}

object CommodityDetails extends DeclarationPage with FieldMapping {

  implicit val format = Json.format[CommodityDetails]

  val pointer: ExportsFieldPointer = "commodityDetails"
  val combinedNomenclatureCodePointer: ExportsFieldPointer = "combinedNomenclatureCode"
  val descriptionOfGoodsPointer: ExportsFieldPointer = "descriptionOfGoods"

  lazy val keyForCode = s"$itemsPrefix.commodityCode"
  lazy val keyForDescription = s"$itemsPrefix.goodsDescription"

  val placeholder = "NNNNNNNNNN"

  val combinedNomenclatureCodeKey = "combinedNomenclatureCode"
  val descriptionOfGoodsKey = "descriptionOfGoods"
  val descriptionOfGoodsMaxLength = 280
  val commodityCodeChemicalPrefixes = Seq(28, 29, 38)

  private val combinedNomenclatureCodeAcceptedLengths = List(8, 10)

  private def mappingCombinedNomenclatureCodeRequired: Mapping[Option[String]] =
    mappingCombinedNomenclatureCodeOptional
      .verifying("declaration.commodityDetails.combinedNomenclatureCode.error.empty", isPresent)

  private def mappingCombinedNomenclatureCodeOptional: Mapping[Option[String]] =
    optional(
      text()
        .transform(_.trim, identity[String])
        .verifying("declaration.commodityDetails.combinedNomenclatureCode.error.empty", nonEmpty)
        .verifying("declaration.commodityDetails.combinedNomenclatureCode.error.invalid", isEmpty or isNumeric)
        .verifying(
          "declaration.commodityDetails.combinedNomenclatureCode.error.length",
          isEmpty or hasSpecificLengths(combinedNomenclatureCodeAcceptedLengths)
        )
    )

  private val mappingDescriptionOfGoodsRequired =
    optional(
      text()
        .verifying("declaration.commodityDetails.description.error.empty", nonEmpty)
        .verifying("declaration.commodityDetails.description.error.length", isEmpty or noLongerThan(descriptionOfGoodsMaxLength))
    ).verifying("declaration.commodityDetails.description.error.empty", isPresent)

  private val mappingDescriptionOfGoodsOptional =
    optional(
      text()
        .verifying("declaration.commodityDetails.description.error.empty", nonEmpty)
        .verifying("declaration.commodityDetails.description.error.length", isEmpty or noLongerThan(descriptionOfGoodsMaxLength))
    )

  private val mappingRequiredCode: Mapping[CommodityDetails] =
    mapping(combinedNomenclatureCodeKey -> mappingCombinedNomenclatureCodeRequired, descriptionOfGoodsKey -> mappingDescriptionOfGoodsRequired)(
      CommodityDetails.apply
    )(CommodityDetails.unapply)

  private val mappingOptionalCode: Mapping[CommodityDetails] =
    mapping(combinedNomenclatureCodeKey -> mappingCombinedNomenclatureCodeOptional, descriptionOfGoodsKey -> mappingDescriptionOfGoodsRequired)(
      CommodityDetails.apply
    )(CommodityDetails.unapply)

  private val mappingOptionalCodeAndOptionalDescription: Mapping[CommodityDetails] =
    mapping(combinedNomenclatureCodeKey -> mappingCombinedNomenclatureCodeOptional, descriptionOfGoodsKey -> mappingDescriptionOfGoodsOptional)(
      CommodityDetails.apply
    )(CommodityDetails.unapply)

  def form(declarationType: DeclarationType): Form[CommodityDetails] = declarationType match {
    case DeclarationType.CLEARANCE                               => Form(mappingOptionalCodeAndOptionalDescription)
    case DeclarationType.SIMPLIFIED | DeclarationType.OCCASIONAL => Form(mappingOptionalCode)
    case _                                                       => Form(mappingRequiredCode)
  }

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    decType match {
      case CLEARANCE =>
        Seq(
          TariffContentKey("tariff.declaration.item.commodityDetails.1.clearance"),
          TariffContentKey("tariff.declaration.item.commodityDetails.2.clearance")
        )
      case _ =>
        Seq(
          TariffContentKey("tariff.declaration.item.commodityDetails.1.common"),
          TariffContentKey("tariff.declaration.item.commodityDetails.2.common")
        )
    }
}
