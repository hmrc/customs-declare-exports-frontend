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

import base.UnitSpec
import forms.common.DeclarationPageBaseSpec
import models.viewmodels.TariffContentKey
import play.api.data.Form

class CommodityMeasureSpec extends UnitSpec with DeclarationPageBaseSpec {

  private def form(grossMass: String, netMass: String): Form[CommodityMeasure] =
    CommodityMeasure.form.bind(Map("grossMass" -> grossMass, "netMass" -> netMass))

  "Commodity Measure form" should {

    "have no errors" when {

      "user fill net and gross mass with correct values" in {
        form("124.123", "123.123").errors must be(empty)
      }

      "the user does not enter decimal digits" in {
        form("124", "123").errors must be(empty)
      }

      "the user does not enter decimal digits after the decimal separator" in {
        form("124.", "123.").errors must be(empty)
      }

      "net and gross mass are equal" in {
        form("12345678.123", "12345678.123").errors must be(empty)
      }

      "net and gross mass are empty" in {
        form("", "").errors must be(empty)
      }

      "user provided net mass only" in {
        form("", "124.123").errors must be(empty)
      }

      "user provided gross mass only" in {
        form("123.123", "").errors must be(empty)
      }
    }

    "have errors" when {

      "the user enters too many decimal digits" in {
        val result = form("12345.1234", "1234.1234")

        val expectedErrorKeys = List("grossMass", "netMass")
        val errorKeys = result.errors.map(_.key)
        errorKeys must be(expectedErrorKeys)

        val errorMessages = result.errors.map(_.message)
        val expectedErrorMessages = List("declaration.commodityMeasure.error", "declaration.commodityMeasure.error")
        errorMessages must be(expectedErrorMessages)
      }

      "the user enters too many digits" in {
        val result = form("123456789.123", "123456789.123")

        val expectedErrorKeys = List("grossMass", "netMass")
        val errorKeys = result.errors.map(_.key)
        errorKeys must be(expectedErrorKeys)

        val errorMessages = result.errors.map(_.message)
        val expectedErrorMessages = List("declaration.commodityMeasure.error", "declaration.commodityMeasure.error")
        errorMessages must be(expectedErrorMessages)
      }

      "the user enters more than one decimal separator" in {
        val result = form("123.45.12", "12.34.12")

        val expectedErrorKeys = List("grossMass", "netMass")
        val errorKeys = result.errors.map(_.key)
        errorKeys must be(expectedErrorKeys)

        val errorMessages = result.errors.map(_.message)
        val expectedErrorMessages = List("declaration.commodityMeasure.error", "declaration.commodityMeasure.error")
        errorMessages must be(expectedErrorMessages)
      }

      "the user enters non-digit characters" in {
        val result = form("1234A89", "126B789.12")

        val expectedErrorKeys = List("grossMass", "netMass")
        val errorKeys = result.errors.map(_.key)
        errorKeys must be(expectedErrorKeys)

        val errorMessages = result.errors.map(_.message)
        val expectedErrorMessages = List("declaration.commodityMeasure.error", "declaration.commodityMeasure.error")
        errorMessages must be(expectedErrorMessages)
      }

      "net mass is greater than gross mass" in {
        val result = form("123.123", "124.123")

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
