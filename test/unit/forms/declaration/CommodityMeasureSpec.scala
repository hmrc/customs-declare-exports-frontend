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

package unit.forms.declaration

import forms.declaration.CommodityMeasure
import org.scalatest.{MustMatchers, WordSpec}

class CommodityMeasureSpec extends WordSpec with MustMatchers {

  val form = CommodityMeasure.form

  "Commodity Measure" should {

    "has correct form id" in {

      CommodityMeasure.commodityFormId must be("CommodityMeasure")
    }
  }

  "Commodity Measure form" should {

    "has no errors" when {

      "user fill only mandatory fields with correct values" in {

        val correctForm = CommodityMeasure(None, "123.12", "123.12")

        val result = form.fillAndValidate(correctForm)

        result.errors must be(empty)
      }

      "user fill all fields with correct values" in {

        val correctForm = CommodityMeasure(Some("1231.12"), "123.12", "123.12")

        val result = form.fillAndValidate(correctForm)

        result.errors must be(empty)
      }
    }

    "has errors" when {

      "mandatory fields are empty" in {

        val incorrectForm = Map("supplementaryUnits" -> "", "grossMass" -> "", "netMass" -> "")

        val result = form.bind(incorrectForm)
        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)

        errorKeys must be(List("grossMass", "netMass"))
        errorMessages must be(List("supplementary.commodityMeasure.grossMass.empty", "supplementary.commodityMeasure.netMass.empty"))
      }

      "data provided by user is incorrect" in {

        val incorrectForm =
          Map("supplementaryUnits" -> "12345.123", "grossMass" -> "12345.123", "netMass" -> "12345.1234")

        val result = form.bind(incorrectForm)
        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)

        errorKeys must be(List("supplementaryUnits", "grossMass", "netMass"))
        errorMessages must be(
          List(
            "supplementary.commodityMeasure.supplementaryUnits.error",
            "supplementary.commodityMeasure.grossMass.error",
            "supplementary.commodityMeasure.netMass.error"
          )
        )
      }
    }
  }
}
