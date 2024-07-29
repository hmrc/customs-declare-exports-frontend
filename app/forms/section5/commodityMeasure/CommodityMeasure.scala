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

package forms.section5.commodityMeasure

import forms.DeclarationPage
import forms.mappings.CrossFieldFormatter
import models.DeclarationType.{CLEARANCE, DeclarationType, STANDARD, SUPPLEMENTARY}
import models.declaration.{CommodityMeasure => CM}
import models.viewmodels.TariffContentKey
import play.api.data.Forms.of
import play.api.data.{Form, Forms}
import utils.validators.forms.FieldValidator._

import scala.util.Try

case class CommodityMeasure(grossMass: Option[String], netMass: Option[String])

object CommodityMeasure extends DeclarationPage {

  def apply(cm: CM): CommodityMeasure =
    new CommodityMeasure(cm.grossMass, cm.netMass)

  def apply(grossMass: String, netMass: String): CommodityMeasure =
    new CommodityMeasure(if (grossMass.isEmpty) None else Some(grossMass), if (netMass.isEmpty) None else Some(netMass))

  def unapply(commodityMeasure: CommodityMeasure): Option[(String, String)] =
    Some((commodityMeasure.grossMass.getOrElse(""), commodityMeasure.netMass.getOrElse("")))

  def form(declarationType: DeclarationType): Form[CommodityMeasure] =
    declarationType match {
      case STANDARD | SUPPLEMENTARY => Form(requiredMapping)
      case _                        => Form(optionalMapping)
    }

  private val massFormatValidation: String => Boolean =
    str => validateDecimalGreaterThanZero(11)(3)(str) and containsNotOnlyZeros(str)

  private val requiredMapping = Forms.mapping(
    "grossMass" -> of(
      CrossFieldFormatter(
        secondaryKey = "",
        constraints = List(
          ("declaration.commodityMeasure.empty", (gross, _) => nonEmpty(gross)),
          ("declaration.commodityMeasure.error", (gross, _) => isEmpty(gross) or massFormatValidation(gross))
        )
      )
    ),
    "netMass" -> of(
      CrossFieldFormatter(
        secondaryKey = "grossMass",
        constraints = List(
          ("declaration.commodityMeasure.empty", (net, _) => nonEmpty(net)),
          ("declaration.commodityMeasure.error", (net, _) => isEmpty(net) or massFormatValidation(net)),
          (
            "declaration.commodityMeasure.netMass.error.biggerThanGrossMass",
            (net: String, gross: String) => isEmpty(net) or isEmpty(gross) or !massFormatValidation(net) or isFirstSmallerOrEqual(net, gross)
          )
        )
      )
    )
  )(CommodityMeasure.apply)(CommodityMeasure.unapply)

  private val optionalMapping = Forms.mapping(
    "grossMass" -> of(
      CrossFieldFormatter(
        secondaryKey = "",
        constraints = List(("declaration.commodityMeasure.error", (gross, _) => isEmpty(gross) or massFormatValidation(gross)))
      )
    ),
    "netMass" -> of(
      CrossFieldFormatter(
        secondaryKey = "grossMass",
        constraints = List(
          ("declaration.commodityMeasure.error", (net, _) => isEmpty(net) or massFormatValidation(net)),
          (
            "declaration.commodityMeasure.netMass.error.biggerThanGrossMass",
            (net, gross) => isEmpty(net) or isEmpty(gross) or !massFormatValidation(net) or isFirstSmallerOrEqual(net, gross)
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
    if (declarationType == CLEARANCE) List(TariffContentKey("tariff.declaration.item.commodityMeasure.clearance"))
    else
      List(
        TariffContentKey("tariff.declaration.item.commodityMeasure.1.common"),
        TariffContentKey("tariff.declaration.item.commodityMeasure.2.common"),
        TariffContentKey("tariff.declaration.item.commodityMeasure.3.common")
      )
}
