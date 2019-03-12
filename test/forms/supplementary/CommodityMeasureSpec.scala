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

import base.CustomExportsBaseSpec
import forms.FormMatchers
import forms.supplementary.CommodityMeasure._
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.choose
import org.scalatest.MustMatchers
import org.scalatest.prop.PropertyChecks
import play.api.data.Form

class CommodityMeasureSpec
    extends CustomExportsBaseSpec with MustMatchers with PropertyChecks with Generators with FormMatchers {

  "CommodityMeasureSpec" should {

    "bind" when {

      "valid values are bound" in {

        forAll { measure: CommodityMeasure =>
          Form(CommodityMeasure.mapping)
            .fillAndValidate(measure)
            .fold(_ => fail("form should not fail"), success => success mustBe measure)
        }
      }
    }

    "fail" when {

      "supplementaryUnits has a precision greater than 16" in {

        forAll(arbitrary[CommodityMeasure], decimal(17, 30, 0)) { (measure, unit) =>
          val data = measure.copy(supplementaryUnits = Some(unit.toString))
          Form(mapping)
            .fillAndValidate(data)
            .fold(
              _ must haveErrorMessage(
                "Supplementary units should be a decimal value and should be less than or equal to 99999999999999.99 with 2 decimals"
              ),
              _ => fail("form should not succeed")
            )
        }
      }

      "supplementaryUnits has a scale greater than 2" in {
        val badData =
          forAll(arbitrary[CommodityMeasure], choose(4, 10).flatMap(posDecimal(16, _))) { (measure, badData) =>
            val data = measure.copy(supplementaryUnits = Some(badData.toString))
            Form(mapping)
              .fillAndValidate(data)
              .fold(
                _ must haveErrorMessage(
                  "Supplementary units should be a decimal value and should be less than or equal to 99999999999999.99 with 2 decimals"
                ),
                _ => fail("form should not succeed")
              )
          }
      }

      "netMass has a precision greater than 16" in {

        forAll(arbitrary[CommodityMeasure], decimal(17, 30, 0)) { (measure, unit) =>
          val data = measure.copy(netMass = unit.toString)
          Form(mapping)
            .fillAndValidate(data)
            .fold(
              _ must haveErrorMessage(
                "Net weight should be a decimal value and should be less than or equal to 99999999999.999 with 3 decimals"
              ),
              _ => fail("form should not succeed")
            )
        }
      }

      "netMass has a scale greater than 2" in {
        val badData =
          forAll(arbitrary[CommodityMeasure], choose(4, 10).flatMap(posDecimal(11, _))) { (measure, badData) =>
            val data = measure.copy(netMass = badData.toString)
            Form(mapping)
              .fillAndValidate(data)
              .fold(
                _ must haveErrorMessage(
                  "Net weight should be a decimal value and should be less than or equal to 99999999999.999 with 3 decimals"
                ),
                _ => fail("form should not succeed")
              )
          }
      }

      "grossMass is empty" in {

        forAll(arbitrary[CommodityMeasure]) { (measure) =>
          val data = measure.copy(grossMass = "")
          Form(mapping)
            .fillAndValidate(data)
            .fold(_ must haveErrorMessage("Gross weight cannot be empty"), _ => fail("form should not succeed"))
        }
      }
      "grossMass has a precision greater than 16" in {

        forAll(arbitrary[CommodityMeasure], decimal(17, 30, 0)) { (measure, unit) =>
          val data = measure.copy(grossMass = unit.toString)
          Form(mapping)
            .fillAndValidate(data)
            .fold(
              _ must haveErrorMessage(
                "Gross weight should be a decimal value and should be less than or equal to 99999999999999.99 with 2 decimals"
              ),
              _ => fail("form should not succeed")
            )
        }
      }

      "grossMass has a scale greater than 2" in {
        val badData =
          forAll(arbitrary[CommodityMeasure], choose(4, 10).flatMap(posDecimal(16, _))) { (measure, badData) =>
            val data = measure.copy(grossMass = badData.toString)
            Form(mapping)
              .fillAndValidate(data)
              .fold(
                _ must haveErrorMessage(
                  "Gross weight should be a decimal value and should be less than or equal to 99999999999999.99 with 2 decimals"
                ),
                _ => fail("form should not succeed")
              )
          }
      }
    }
  }
}
