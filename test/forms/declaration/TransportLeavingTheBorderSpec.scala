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
import forms.declaration.ModeOfTransportCode.{meaningfulModeOfTransportCodes, Empty, RoRo}
import forms.declaration.TransportLeavingTheBorder.{errorKey, suffixForLocationOfGoods}
import org.scalatest.EitherValues

class TransportLeavingTheBorderSpec extends DeclarationPageBaseSpec with JourneyTypeTestRunner with EitherValues {

  "TransportLeavingTheBorder's mapping for non-Clearance journeys" should {

    "return no errors" when {

      meaningfulModeOfTransportCodes.foreach { modeOfTransportCode =>
        s"ModeOfTransportCode is $modeOfTransportCode and" when {
          val input = Map("transportLeavingTheBorder" -> modeOfTransportCode.value)

          "LocationOfGood's value is None" in {
            val result = TransportLeavingTheBorder.mapping(false, None).bind(input)

            result.isRight mustBe true
            result.value mustBe TransportLeavingTheBorder(Some(modeOfTransportCode))
          }

          s"LocationOfGood's value is present but does not end with '$suffixForLocationOfGoods'" in {
            val result = TransportLeavingTheBorder.mapping(false, Some(LocationOfGoods("GBAUFEMLHRGGG"))).bind(input)

            result.isRight mustBe true
            result.value mustBe TransportLeavingTheBorder(Some(modeOfTransportCode))
          }
        }
      }

      "ModeOfTransportCode is RoRo and" when {
        s"LocationOfGood's value is present and does end with '$suffixForLocationOfGoods'" in {
          val input = Map("transportLeavingTheBorder" -> RoRo.value)
          val locationOfGoods = Some(LocationOfGoods(s"GBAUFEMLHR$suffixForLocationOfGoods"))

          val result = TransportLeavingTheBorder.mapping(false, locationOfGoods).bind(input)

          result.isRight mustBe true
          result.value mustBe TransportLeavingTheBorder(Some(ModeOfTransportCode.RoRo))
        }
      }
    }
  }

  "TransportLeavingTheBorder's mapping for non-Clearance journeys" should {

    "return errors" when {

      "provided with an empty String" in {
        val input = Map("transportLeavingTheBorder" -> "")

        val result = TransportLeavingTheBorder.mapping(false, None).bind(input)

        result.isLeft mustBe true
        result.left.get.head.message mustBe s"$errorKey.empty"
      }

      "provided with the 'Empty' ModeOfTransportCode value" in {
        val input = Map("transportLeavingTheBorder" -> Empty.value)

        val result = TransportLeavingTheBorder.mapping(false, None).bind(input)

        result.isLeft mustBe true
        result.left.get.head.message mustBe s"$errorKey.incorrect"
      }

      "provided with an invalid ModeOfTransportCode value" in {
        val input = Map("transportLeavingTheBorder" -> "123")

        val result = TransportLeavingTheBorder.mapping(false, None).bind(input)

        result.isLeft mustBe true
        result.left.get.head.message mustBe s"$errorKey.incorrect"
      }

      s"LocationOfGood's value is present and does end with '$suffixForLocationOfGoods' and" when {
        val locationOfGoods = Some(LocationOfGoods(s"GBAUFEMLHR$suffixForLocationOfGoods"))

        meaningfulModeOfTransportCodes.filter(_ != RoRo).foreach { modeOfTransportCode =>
          s"ModeOfTransportCode is $modeOfTransportCode" in {
            val input = Map("transportLeavingTheBorder" -> modeOfTransportCode.value)

            val result = TransportLeavingTheBorder.mapping(false, locationOfGoods).bind(input)
            result.left.get.head.message mustBe s"$errorKey.roro.required"
          }
        }
      }
    }
  }

  "TransportLeavingTheBorder's mapping for the Clearance journey" should {

    "return no errors" when {

      (meaningfulModeOfTransportCodes + Empty).foreach { modeOfTransportCode =>
        s"ModeOfTransportCode is $modeOfTransportCode and" when {
          val input = Map("transportLeavingTheBorder" -> modeOfTransportCode.value)

          "LocationOfGood's value is None" in {
            val result = TransportLeavingTheBorder.mapping(true, None).bind(input)

            result.isRight mustBe true
            result.value mustBe TransportLeavingTheBorder(Some(modeOfTransportCode))
          }

          s"LocationOfGood's value is present but does not end with '$suffixForLocationOfGoods'" in {
            val result = TransportLeavingTheBorder.mapping(true, Some(LocationOfGoods("GBAUFEMLHRGGG"))).bind(input)

            result.isRight mustBe true
            result.value mustBe TransportLeavingTheBorder(Some(modeOfTransportCode))
          }
        }
      }

      "ModeOfTransportCode is RoRo and" when {
        s"LocationOfGood's value is present and does end with '$suffixForLocationOfGoods'" in {
          val input = Map("transportLeavingTheBorder" -> RoRo.value)
          val locationOfGoods = Some(LocationOfGoods(s"GBAUFEMLHR$suffixForLocationOfGoods"))

          val result = TransportLeavingTheBorder.mapping(true, locationOfGoods).bind(input)

          result.isRight mustBe true
          result.value mustBe TransportLeavingTheBorder(Some(ModeOfTransportCode.RoRo))
        }
      }
    }

    "return errors" when {

      "provided with an empty String" in {
        val input = Map("transportLeavingTheBorder" -> "")

        val result = TransportLeavingTheBorder.mapping(true, None).bind(input)

        result.isLeft mustBe true
        result.left.get.head.message mustBe s"$errorKey.empty.optional"
      }

      "provided with an invalid ModeOfTransportCode value" in {
        val input = Map("transportLeavingTheBorder" -> "123")

        val result = TransportLeavingTheBorder.mapping(true, None).bind(input)

        result.isLeft mustBe true
        result.left.get.head.message mustBe s"$errorKey.incorrect"
      }

      s"LocationOfGood's value is present and does end with '$suffixForLocationOfGoods' and" when {
        val locationOfGoods = Some(LocationOfGoods(s"GBAUFEMLHR$suffixForLocationOfGoods"))

        (meaningfulModeOfTransportCodes + Empty).filter(_ != RoRo).foreach { modeOfTransportCode =>
          s"ModeOfTransportCode is $modeOfTransportCode" in {
            val input = Map("transportLeavingTheBorder" -> modeOfTransportCode.value)

            val result = TransportLeavingTheBorder.mapping(true, locationOfGoods).bind(input)
            result.left.get.head.message mustBe s"$errorKey.roro.required"
          }
        }
      }
    }
  }

  "TransportLeavingTheBorder" when {
    testTariffContentKeys(TransportLeavingTheBorder, "tariff.declaration.transportLeavingTheBorder")
  }
}
