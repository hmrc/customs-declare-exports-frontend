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

package forms.declaration.commodityMeasure

import base.UnitSpec
import forms.common.DeclarationPageBaseSpec
import models.viewmodels.TariffContentKey
import play.api.data.Form

class CommodityMeasureSpec extends UnitSpec with DeclarationPageBaseSpec {

  private def form(grossMass: String, netMass: String): Form[CommodityMeasure] =
    CommodityMeasure.form.bind(Map("grossMass" -> grossMass, "netMass" -> netMass))

  "Commodity Measure form" should {

    "have no errors" when {

      "user fill mandatory fields with correct values" in {
        form("124.12", "123.12").errors must be(empty)
      }

      "net and gross mass are equal" in {
        form("123.12", "123.12").errors must be(empty)
      }

      "mandatory fields are empty" in {
        form("", "").errors must be(empty)
      }

      "user provided net mass only" in {
        form("", "124.12").errors must be(empty)
      }

      "user provided gross mass only" in {
        form("123.12", "").errors must be(empty)
      }
    }

    "have errors" when {

      "data provided by user is incorrect" in {
        val result = form("12345.12333333", "1234.533333333331234")

        val expectedErrorKeys = List("grossMass", "netMass")
        val errorKeys = result.errors.map(_.key)
        errorKeys must be(expectedErrorKeys)

        val errorMessages = result.errors.map(_.message)
        val expectedErrorMessages = List("declaration.commodityMeasure.grossMass.error", "declaration.commodityMeasure.netMass.error")
        errorMessages must be(expectedErrorMessages)
      }

      "net mass is greater than gross mass" in {
        val result = form("123.12", "124.12")

        val errorKeys = result.errors.map(_.key)
        val expectedErrorKeys = List("netMass")
        errorKeys must be(expectedErrorKeys)

        val errorMessages = result.errors.map(_.message)
        val expectedErrorMessages = List("declaration.commodityMeasure.netMass.error.biggerThanGrossMass")
        errorMessages must be(expectedErrorMessages)
      }
    }
  }

  override def getCommonTariffKeys(messageKey: String): Seq[TariffContentKey] =
    List(TariffContentKey(s"${messageKey}.1.common"), TariffContentKey(s"${messageKey}.2.common"), TariffContentKey(s"${messageKey}.3.common"))

  override def getClearanceTariffKeys(messageKey: String): Seq[TariffContentKey] =
    List(TariffContentKey(s"${messageKey}.clearance"))

  "CommodityMeasure" when {
    testTariffContentKeys(CommodityMeasure, "tariff.declaration.item.commodityMeasure")
  }
}
