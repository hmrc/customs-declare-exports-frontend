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

package forms.declaration
import forms.DeclarationPage
import models.DeclarationType
import models.DeclarationType.DeclarationType
import play.api.data.Forms.{mapping, optional, text}
import play.api.data.{Form, Mapping}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class CommodityDetails(combinedNomenclatureCode: Option[String], descriptionOfGoods: String)

object CommodityDetails extends DeclarationPage {

  implicit val format = Json.format[CommodityDetails]

  val combinedNomenclatureCodeKey = "combinedNomenclatureCode"
  val descriptionOfGoodsKey = "descriptionOfGoods"

  private val combinedNomenclatureCodeMaxLength = 8
  private val descriptionOfGoodsMaxLength = 280

  private def mappingCombinedNomenclatureCodeRequired: Mapping[Option[String]] =
    optional(
      text()
        .verifying("declaration.commodityDetails.combinedNomenclatureCode.error.empty", nonEmpty)
        .verifying("declaration.commodityDetails.combinedNomenclatureCode.error.length", isEmpty or noLongerThan(combinedNomenclatureCodeMaxLength))
        .verifying("declaration.commodityDetails.combinedNomenclatureCode.error.specialCharacters", isEmpty or isNumeric)
    ).verifying("declaration.commodityDetails.combinedNomenclatureCode.error.empty", isPresent)

  private def mappingCombinedNomenclatureCodeOptional: Mapping[Option[String]] =
    optional(
      text()
        .verifying("declaration.commodityDetails.combinedNomenclatureCode.error.empty", nonEmpty)
        .verifying("declaration.commodityDetails.combinedNomenclatureCode.error.length", isEmpty or noLongerThan(combinedNomenclatureCodeMaxLength))
        .verifying("declaration.commodityDetails.combinedNomenclatureCode.error.specialCharacters", isEmpty or isNumeric)
    )

  private val mappingDescriptionOfGoods = text()
    .verifying("declaration.commodityDetails.description.error.empty", nonEmpty)
    .verifying("declaration.commodityDetails.description.error.length", isEmpty or noLongerThan(descriptionOfGoodsMaxLength))

  private val mappingRequiredCode: Mapping[CommodityDetails] =
    mapping(combinedNomenclatureCodeKey -> mappingCombinedNomenclatureCodeRequired, descriptionOfGoodsKey -> mappingDescriptionOfGoods)(
      CommodityDetails.apply
    )(CommodityDetails.unapply)

  private val mappingOptionalCode: Mapping[CommodityDetails] =
    mapping(combinedNomenclatureCodeKey -> mappingCombinedNomenclatureCodeOptional, descriptionOfGoodsKey -> mappingDescriptionOfGoods)(
      CommodityDetails.apply
    )(CommodityDetails.unapply)

  def form(declarationType: DeclarationType): Form[CommodityDetails] = declarationType match {
    case DeclarationType.SIMPLIFIED => Form(mappingOptionalCode)
    case _                          => Form(mappingRequiredCode)
  }
}
