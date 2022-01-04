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

package forms.declaration
import forms.DeclarationPage
import models.DeclarationType
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.viewmodels.TariffContentKey
import play.api.data.{Form, Mapping}
import play.api.data.Forms.{mapping, optional, text}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class CommodityDetails(combinedNomenclatureCode: Option[String], descriptionOfGoods: Option[String])

object CommodityDetails extends DeclarationPage {

  implicit val format = Json.format[CommodityDetails]

  val placeholder = "NNNNNNNNNN"

  val combinedNomenclatureCodeKey = "combinedNomenclatureCode"
  val descriptionOfGoodsKey = "descriptionOfGoods"
  val descriptionOfGoodsMaxLength = 280
  val commodityCodeChemicalPrefixes = Seq(28, 29, 38)

  private val combinedNomenclatureCodeMaxLength = 10

  private def mappingCombinedNomenclatureCodeRequired: Mapping[Option[String]] =
    mappingCombinedNomenclatureCodeOptional
      .verifying("declaration.commodityDetails.combinedNomenclatureCode.error.empty", isPresent)

  private def mappingCombinedNomenclatureCodeOptional: Mapping[Option[String]] =
    optional(
      text()
        .verifying("declaration.commodityDetails.combinedNomenclatureCode.error.empty", nonEmpty)
        .verifying("declaration.commodityDetails.combinedNomenclatureCode.error.invalid", isEmpty or isNumeric)
        .verifying(
          "declaration.commodityDetails.combinedNomenclatureCode.error.length",
          isEmpty or hasSpecificLength(combinedNomenclatureCodeMaxLength)
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
          TariffContentKey("tariff.declaration.item.commodityDetails.2.clearance"),
          TariffContentKey("tariff.declaration.item.commodityDetails.3.clearance")
        )
      case _ =>
        Seq(
          TariffContentKey("tariff.declaration.item.commodityDetails.1.common"),
          TariffContentKey("tariff.declaration.item.commodityDetails.2.common")
        )
    }
}
