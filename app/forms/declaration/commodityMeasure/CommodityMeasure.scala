/*
 * Copyright 2021 HM Revenue & Customs
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

package forms.declaration.commodityMeasure

import scala.util.Try

import forms.DeclarationPage
import forms.mappings.CrossFieldFormatter
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.declaration.{CommodityMeasure => CM}
import models.viewmodels.TariffContentKey
import play.api.data.Forms.of
import play.api.data.{Form, Forms}
import utils.validators.forms.FieldValidator._

case class CommodityMeasure(grossMass: Option[String], netMass: Option[String])

object CommodityMeasure extends DeclarationPage {

  def apply(cm: CM): CommodityMeasure =
    new CommodityMeasure(cm.grossMass, cm.netMass)

  def apply(grossMass: String, netMass: String): CommodityMeasure =
    new CommodityMeasure(if (grossMass.isEmpty) None else Some(grossMass), if (netMass.isEmpty) None else Some(netMass))

  def unapply(commodityMeasure: CommodityMeasure): Option[(String, String)] =
    Some((commodityMeasure.grossMass.getOrElse(""), commodityMeasure.netMass.getOrElse("")))

  def form: Form[CommodityMeasure] = Form(mapping)

  private val massFormatValidation: String => Boolean =
    str => validateDecimalGreaterThanZero(16)(6)(str) and containsNotOnlyZeros(str)

  private val mapping = Forms.mapping(
    "grossMass" -> of(
      CrossFieldFormatter(
        secondaryKey = "",
        constraints = Seq(
          ("declaration.commodityMeasure.grossMass.empty", (gross: String, _: String) => nonEmpty(gross)),
          ("declaration.commodityMeasure.grossMass.error", (gross: String, _: String) => isEmpty(gross) or massFormatValidation(gross))
        )
      )
    ),
    "netMass" -> of(
      CrossFieldFormatter(
        secondaryKey = "grossMass",
        constraints = Seq(
          ("declaration.commodityMeasure.netMass.empty", (net: String, _: String) => nonEmpty(net)),
          ("declaration.commodityMeasure.netMass.error", (net: String, _: String) => isEmpty(net) or massFormatValidation(net)),
          (
            "declaration.commodityMeasure.netMass.error.biggerThanGrossMass",
            (net: String, gross: String) => isEmpty(net) or isEmpty(gross) or !massFormatValidation(net) or isFirstSmallerOrEqual(net, gross)
          )
        )
      )
    )
  )(CommodityMeasure.apply)(CommodityMeasure.unapply)

  private def isFirstSmallerOrEqual(first: String, second: String): Boolean =
    Try {
      val firstNum = BigDecimal(first)
      val secondNum = BigDecimal(second)
      firstNum <= secondNum
    }.getOrElse(false)

  override def defineTariffContentKeys(declarationType: DeclarationType): Seq[TariffContentKey] =
    if (declarationType == CLEARANCE)
      Seq(
        TariffContentKey("tariff.declaration.item.commodityMeasure.1.clearance"),
        TariffContentKey("tariff.declaration.item.commodityMeasure.2.clearance")
      )
    else
      Seq(
        TariffContentKey("tariff.declaration.item.commodityMeasure.1.common"),
        TariffContentKey("tariff.declaration.item.commodityMeasure.2.common")
      )
}
