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
import models.DeclarationType
import org.scalatest.{MustMatchers, WordSpec}

class CommodityMeasureSpec extends WordSpec with MustMatchers {

  "Commodity Measure" should {

    "have correct form id" in {

      CommodityMeasure.commodityFormId must be("CommodityMeasure")
    }
  }

  "Commodity Measure default form" should {
    val formDefault = CommodityMeasure.form(DeclarationType.STANDARD)

    "have no errors" when {

      "user fill only mandatory fields with correct values" in {

        val correctForm = Map("grossMass" -> "124.12", "netMass" -> "123.12")

        val result = formDefault.bind(correctForm)

        result.errors must be(empty)
      }

      "net and gross mass are equal" in {

        val correctForm = Map("grossMass" -> "123.12", "netMass" -> "123.12")

        val result = formDefault.bind(correctForm)

        result.errors must be(empty)
      }

      "user fill all fields with correct values" in {

        val correctForm = Map("supplementaryUnits" -> "1231.12", "grossMass" -> "124.12", "netMass" -> "123.12")

        val result = formDefault.bind(correctForm)

        result.errors must be(empty)
      }
    }

    "have errors" when {

      "mandatory fields are empty" in {

        val incorrectForm = Map("supplementaryUnits" -> "", "grossMass" -> "", "netMass" -> "")

        val result = formDefault.bind(incorrectForm)

        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)
        val expectedErrorKeys = List("grossMass", "netMass")
        val expectedErrorMessages = List("declaration.commodityMeasure.grossMass.empty", "declaration.commodityMeasure.netMass.empty")

        errorKeys must be(expectedErrorKeys)
        errorMessages must be(expectedErrorMessages)
      }

      "no mandatory fields are present" in {

        val incorrectForm = Map.empty[String, String]

        val result = formDefault.bind(incorrectForm)

        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)
        val expectedErrorKeys = List("grossMass", "netMass")
        val expectedErrorMessages = List("declaration.commodityMeasure.grossMass.empty", "declaration.commodityMeasure.netMass.empty")

        errorKeys must be(expectedErrorKeys)
        errorMessages must be(expectedErrorMessages)
      }

      "data provided by user is incorrect" in {

        val incorrectForm = Map("supplementaryUnits" -> "0", "grossMass" -> "12345.12333333", "netMass" -> "1234.533333333331234")

        val result = formDefault.bind(incorrectForm)

        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)
        val expectedErrorKeys = List("supplementaryUnits", "grossMass", "netMass")
        val expectedErrorMessages = List(
          "declaration.commodityMeasure.supplementaryUnits.error",
          "declaration.commodityMeasure.grossMass.error",
          "declaration.commodityMeasure.netMass.error.format"
        )

        errorKeys must be(expectedErrorKeys)
        errorMessages must be(expectedErrorMessages)
      }

      "net mass is greater than gross mass" in {

        val incorrectForm = Map("grossMass" -> "123.12", "netMass" -> "124.12")

        val result = formDefault.bind(incorrectForm)

        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)
        val expectedErrorKeys = List("netMass")
        val expectedErrorMessages = List("declaration.commodityMeasure.netMass.error.biggerThanGrossMass")

        errorKeys must be(expectedErrorKeys)
        errorMessages must be(expectedErrorMessages)
      }
    }
  }

  "Commodity Measure clearance form" should {
    val formClearance = CommodityMeasure.form(DeclarationType.CLEARANCE)

    "have no errors" when {

      "user fills all fields with correct values" in {

        val correctForm = Map("grossMass" -> "124.12", "netMass" -> "123.12")

        val result = formClearance.bind(correctForm)

        result.errors must be(empty)
      }

      "user fills net mass only" in {

        val correctForm = Map("netMass" -> "123.12")

        val result = formClearance.bind(correctForm)

        result.errors must be(empty)
      }

      "user fills gross mass only" in {

        val correctForm = Map("grossMass" -> "124.12")

        val result = formClearance.bind(correctForm)

        result.errors must be(empty)
      }

      "net and gross mass are equal" in {

        val correctForm = Map("grossMass" -> "123.12", "netMass" -> "123.12")

        val result = formClearance.bind(correctForm)

        result.errors must be(empty)
      }

      "all fields are empty" in {

        val incorrectForm = Map("grossMass" -> "", "netMass" -> "")

        val result = formClearance.bind(incorrectForm)

        result.errors must be(empty)
      }

      "no fields are present" in {

        val incorrectForm = Map.empty[String, String]

        val result = formClearance.bind(incorrectForm)

        result.errors must be(empty)
      }
    }

    "have errors" when {

      "data provided by user is incorrect" in {

        val incorrectForm = Map("grossMass" -> "12345.12333333", "netMass" -> "123453333333333.1234")

        val result = formClearance.bind(incorrectForm)

        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)
        val expectedErrorKeys = List("grossMass", "netMass")
        val expectedErrorMessages = List("declaration.commodityMeasure.grossMass.error", "declaration.commodityMeasure.netMass.error.format")

        errorKeys must be(expectedErrorKeys)
        errorMessages must be(expectedErrorMessages)
      }

      "net mass is greater than gross mass" in {

        val incorrectForm = Map("grossMass" -> "123.12", "netMass" -> "124.12")

        val result = formClearance.bind(incorrectForm)

        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)
        val expectedErrorKeys = List("netMass")
        val expectedErrorMessages = List("declaration.commodityMeasure.netMass.error.biggerThanGrossMass")

        errorKeys must be(expectedErrorKeys)
        errorMessages must be(expectedErrorMessages)
      }
    }
  }
}
