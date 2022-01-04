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

import base.JourneyTypeTestRunner
import forms.common.DeclarationPageBaseSpec
import models.DeclarationType._

class TransportLeavingTheBorderSpec extends DeclarationPageBaseSpec with JourneyTypeTestRunner {

  "TransportLeavingTheBorder classicMapping" should {

    "return Right" when {
      "provided with allowed code" in {

        val input = Map("transportLeavingTheBorder" -> "1")

        val result = TransportLeavingTheBorder.classicMapping.bind(input)

        result.isRight mustBe true
        result.right.get mustBe TransportLeavingTheBorder(Some(ModeOfTransportCode.Maritime))
      }
    }

    "return form with errors" when {

      "provided with empty String" in {

        val input = Map("transportLeavingTheBorder" -> "")

        val result = TransportLeavingTheBorder.classicMapping.bind(input)

        result.isLeft mustBe true
        result.left.get.head.message mustBe "declaration.transportInformation.borderTransportMode.error.empty"
      }

      "provided with nocode value" in {

        val input = Map("transportLeavingTheBorder" -> "nocode")

        val result = TransportLeavingTheBorder.classicMapping.bind(input)

        result.isLeft mustBe true
        result.left.get.head.message mustBe "declaration.transportInformation.borderTransportMode.error.incorrect"
      }

      "provided with not allowed code" in {

        val input = Map("transportLeavingTheBorder" -> "123")

        val result = TransportLeavingTheBorder.classicMapping.bind(input)

        result.isLeft mustBe true
        result.left.get.head.message mustBe "declaration.transportInformation.borderTransportMode.error.incorrect"
      }
    }
  }

  "TransportLeavingTheBorder clearanceMapping" should {

    "return Right" when {

      "provided with nocode value" in {

        val input = Map("transportLeavingTheBorder" -> "no-code")

        val result = TransportLeavingTheBorder.clearanceMapping.bind(input)

        result.isRight mustBe true
        result.right.get mustBe TransportLeavingTheBorder(Some(ModeOfTransportCode.Empty))
      }

      "provided with allowed code" in {

        val input = Map("transportLeavingTheBorder" -> "1")

        val result = TransportLeavingTheBorder.clearanceMapping.bind(input)

        result.isRight mustBe true
        result.right.get mustBe TransportLeavingTheBorder(Some(ModeOfTransportCode.Maritime))
      }
    }

    "return form with errors" when {

      "provided with empty String" in {

        val input = Map("transportLeavingTheBorder" -> "")

        val result = TransportLeavingTheBorder.clearanceMapping.bind(input)

        result.isLeft mustBe true
        result.left.get.head.message mustBe "declaration.transportInformation.borderTransportMode.error.empty.optional"
      }

      "provided with not allowed code" in {

        val input = Map("transportLeavingTheBorder" -> "123")

        val result = TransportLeavingTheBorder.clearanceMapping.bind(input)

        result.isLeft mustBe true
        result.left.get.head.message mustBe "declaration.transportInformation.borderTransportMode.error.incorrect"
      }
    }
  }

  "TransportLeavingTheBorder form method" should {

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
      "return classicMapping" in {

        TransportLeavingTheBorder.form(request.declarationType).mapping mustBe TransportLeavingTheBorder.classicMapping
      }
    }

    onClearance { request =>
      "return clearanceMapping" in {

        TransportLeavingTheBorder.form(request.declarationType).mapping mustBe TransportLeavingTheBorder.clearanceMapping
      }
    }
  }

  "TransportLeavingTheBorder" when {
    testTariffContentKeys(TransportLeavingTheBorder, "tariff.declaration.transportLeavingTheBorder")
  }
}
