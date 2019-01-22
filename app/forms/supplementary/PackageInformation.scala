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

package forms.supplementary

import play.api.data.{Form, Forms}
import play.api.data.Forms.{nonEmptyText, text}
import play.api.libs.json.Json
import utils.validators.FormFieldValidator._

case class PackageInformation(
  typesOfPackages: String,
  numberOfPackages: String,
  supplementaryUnits: String,
  shippingMarks: String,
  netMass: String,
  grossMass: String
)

object PackageInformation {

  implicit val format = Json.format[PackageInformation]

  val formId = "PackageInformation"

  val mapping = Forms.mapping(
    "typesOfPackages" -> nonEmptyText()
      .verifying(
        "supplementary.packageInformation.typesOfPackages.error",
        isAlphanumeric and hasSpecificLength(2) and nonEmpty
      ),
    "numberOfPackages" -> text()
      .verifying("supplementary.packageInformation.numberOfPackages.error", isNumeric and noLongerThan(5) and nonEmpty),
    "supplementaryUnits" -> text().verifying(
      "supplementary.packageInformation.supplementaryUnits.error",
      nonEmpty and (
        (isDecimal and isDecimalNoLongerThan(16) and isDecimalWithNoMoreDecimalPlacesThan(6)) or (isNumeric and noLongerThan(
          10
        ))
      )
    ),
    "shippingMarks" -> text()
      .verifying(
        "supplementary.packageInformation.shippingMarks.error",
        isAlphanumeric and noLongerThan(42) and nonEmpty
      ),
    "netMass" -> text()
      .verifying(
        "supplementary.packageInformation.netMass.error",
        nonEmpty and (
          (isDecimal and isDecimalNoLongerThan(11) and isDecimalWithNoMoreDecimalPlacesThan(3)) or (isNumeric and noLongerThan(
            8
          ))
        )
      ),
    "grossMass" -> text()
      .verifying(
        "supplementary.packageInformation.grossMass.error",
        nonEmpty and (
          (isDecimal and isDecimalNoLongerThan(16) and isDecimalWithNoMoreDecimalPlacesThan(6)) or (isNumeric and noLongerThan(
            10
          ))
        )
      )
  )(PackageInformation.apply)(PackageInformation.unapply)

  def form(): Form[PackageInformation] = Form(mapping)

  def toMetadataProperties(document: PackageInformation): Map[String, String] =
    Map(
      "declaration.goodsShipment.governmentAgencyGoodsItem.packaging.typeCode" ->
        document.typesOfPackages,
      "declaration.goodsShipment.governmentAgencyGoodsItem.packaging.quantityQuantity" ->
        document.numberOfPackages,
      "declaration.goodsShipment.governmentAgencyGoodsItem.packaging.tariffQuantity" ->
        document.supplementaryUnits,
      "declaration.goodsShipment.governmentAgencyGoodsItem.packaging.marksNumbersID" ->
        document.shippingMarks,
      "declaration.goodsShipment.governmentAgencyGoodsItem.packaging.netNetWeightMeasure" ->
        document.netMass,
      "declaration.goodsShipment.governmentAgencyGoodsItem.packaging.grossMassMeasure" ->
        document.grossMass
    )
}
