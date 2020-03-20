/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class CommodityMeasure(supplementaryUnits: Option[String], grossMass: Option[String], netMass: Option[String])

object CommodityMeasure extends DeclarationPage {

  implicit val format = Json.format[CommodityMeasure]

  val commodityFormId = "CommodityMeasure"

  def applyDefault(supplementaryUnits: Option[String], grossMass: String, netMass: String): CommodityMeasure =
    CommodityMeasure(supplementaryUnits, if (grossMass.isEmpty) None else Some(grossMass), if (netMass.isEmpty) None else Some(netMass))

  def unapplyDefault(value: CommodityMeasure): Option[(Option[String], String, String)] =
    Some(value.supplementaryUnits, value.grossMass.getOrElse(""), value.netMass.getOrElse(""))

  def applyClearance(grossMass: Option[String], netMass: Option[String]): CommodityMeasure =
    CommodityMeasure(None, grossMass, netMass)

  def unapplyClearance(value: CommodityMeasure): Option[(Option[String], Option[String])] =
    Some(value.grossMass, value.netMass)

  private val mappingDefault = Forms.mapping(
    "supplementaryUnits" -> optional(
      text().verifying("declaration.commodityMeasure.supplementaryUnits.error", validateDecimalGreaterThanZero(16)(6) and containsNotOnlyZeros)
    ),
    "grossMass" -> text
      .verifying("declaration.commodityMeasure.grossMass.empty", nonEmpty)
      .verifying("declaration.commodityMeasure.grossMass.error", isEmpty or validateDecimalGreaterThanZero(16)(6) and containsNotOnlyZeros),
    "netMass" -> text
      .verifying("declaration.commodityMeasure.netMass.empty", nonEmpty)
      .verifying("declaration.commodityMeasure.netMass.error", isEmpty or validateDecimalGreaterThanZero(16)(6) and containsNotOnlyZeros)
  )(CommodityMeasure.applyDefault)(CommodityMeasure.unapplyDefault)

  private val mappingClearance = Forms.mapping(
    "grossMass" -> optional(
      text()
        .verifying("declaration.commodityMeasure.grossMass.error", isEmpty or validateDecimalGreaterThanZero(16)(6) and containsNotOnlyZeros)
    ),
    "netMass" -> optional(
      text()
        .verifying("declaration.commodityMeasure.netMass.error", isEmpty or validateDecimalGreaterThanZero(16)(6) and containsNotOnlyZeros)
    )
  )(CommodityMeasure.applyClearance)(CommodityMeasure.unapplyClearance)

  def form(declarationType: DeclarationType): Form[CommodityMeasure] = declarationType match {
    case DeclarationType.CLEARANCE => Form(mappingClearance)
    case _                         => Form(mappingDefault)
  }
}
