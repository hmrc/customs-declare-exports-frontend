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

package forms.declaration

import base.CustomExportsBaseSpec
import forms.FormMatchers
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.alphaNumStr
import org.scalatest.MustMatchers
import org.scalatest.prop.PropertyChecks
import play.api.data.Form

class BorderTransportSpec
    extends CustomExportsBaseSpec with MustMatchers with PropertyChecks with Generators with FormMatchers {

  "BorderTransportMapping" should {

    "bind" when {

      "valid values are bound" in {

        forAll { borderTransport: BorderTransport =>
          Form(BorderTransport.formMapping)
            .fillAndValidate(borderTransport)
            .fold(_ => fail("form should not fail"), success => success mustBe borderTransport)
        }
      }
    }

    "fail" when {

      "meansOfTransportOnDepartureIDNumber is longer than 27 characters" in {

        forAll(arbitrary[BorderTransport], alphaNumStr) { (borderTransport, id) =>
        whenever(id.size > 27) {
          val data = borderTransport.copy(meansOfTransportOnDepartureIDNumber = Some(id))
          Form(BorderTransport.formMapping)
            .fillAndValidate(data)
            .fold(
              _ must haveErrorMessage("Reference should be less than 28 alpha numeric characters"),
              _ => fail("should not succeed")
            )
        }

        }
      }
      "meansOfTransportOnDepartureIDNumber has alphaNumeric characters" in {

        forAll(arbitrary[BorderTransport], stringsWithMaxLength(10)) { (borderTransport, id) =>
          val data = borderTransport.copy(meansOfTransportOnDepartureIDNumber = Some(id + ("£$%")))
          Form(BorderTransport.formMapping)
            .fillAndValidate(data)
            .fold(
              _ must haveErrorMessage("Reference cannot contain special characters"),
              _ => fail("should not succeed")
            )
        }
      }


      "borderModeOfTransportCode is not supplied" in {
        Form(BorderTransport.formMapping)
          .bind(Map.empty[String, String])
          .fold(
            _ must haveErrorMessage(
              "Please, choose mode of transport at the border"
            ),
            _ => fail("should not succeed")
          )
      }
       "meansOfTransportOnDepartureType data is not supplied" in {
        Form(BorderTransport.formMapping)
          .bind(Map.empty[String, String])
          .fold(
            _ must haveErrorMessage(
              "Please, choose transport details"
            ),
            _ => fail("should not succeed")
          )
      }
    }
  }
}
