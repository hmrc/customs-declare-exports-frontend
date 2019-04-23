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

class TransportDetailsSpec
    extends CustomExportsBaseSpec with MustMatchers with PropertyChecks with Generators with FormMatchers {

  "TransportDetailsMapping" should {

    "bind" when {

      "valid values are bound" in {

        forAll { transportDetails: TransportDetails =>
          Form(TransportDetails.formMapping)
            .fillAndValidate(transportDetails)
            .fold(_ => fail("form should not fail"), success => success mustBe transportDetails)
        }
      }
    }

    "fail" when {

      "invalid countryCode is supplied" in {

        forAll(arbitrary[TransportDetails], alphaNumStr) { (transportDetails, id) =>
          val data = transportDetails.copy(meansOfTransportCrossingTheBorderNationality = Some(id))
          Form(TransportDetails.formMapping)
            .fillAndValidate(data)
            .fold(
              _ must haveErrorMessage("Please, choose valid active means of transport"),
              _ => fail("should not succeed")
            )
        }
      }

      "meansOfTransportCrossingTheBorderType is not provided" in {

        forAll(arbitrary[TransportDetails]) { (transportDetails) =>
          Form(TransportDetails.formMapping)
            .bind(
              Map[String, String](
                "container" -> "true",
                "meansOfTransportCrossingTheBorderIDNumber" -> transportDetails.meansOfTransportCrossingTheBorderIDNumber
                  .getOrElse("12"),
                "meansOfTransportCrossingTheBorderNationality" -> transportDetails.meansOfTransportCrossingTheBorderNationality
                  .getOrElse("PL")
              )
            )
            .fold(_ must haveErrorMessage("Please, choose active means of transport"), _ => fail("should not succeed"))
        }
      }

      "meansOfTransportCrossingTheBorderIDNumber is longer than 35" in {

        forAll(arbitrary[TransportDetails], stringsLongerThan(36)) { (transportDetails, id) =>
          val data = transportDetails.copy(meansOfTransportCrossingTheBorderIDNumber = Some(id))
          Form(TransportDetails.formMapping)
            .fillAndValidate(data)
            .fold(
              _ must haveErrorMessage("Reference should be less than 35 alpha numeric characters"),
              _ => fail("should not succeed")
            )
        }
      }
      "meansOfTransportCrossingTheBorderIDNumber is with special characters" in {

        forAll(arbitrary[TransportDetails], stringsWithMaxLength(20)) { (transportDetails, id) =>
          val data = transportDetails.copy(meansOfTransportCrossingTheBorderIDNumber = Some(id + "£$%"))
          Form(TransportDetails.formMapping)
            .fillAndValidate(data)
            .fold(
              _ must haveErrorMessage("Reference cannot contain special characters"),
              _ => fail("should not succeed")
            )
        }
      }
      "has containers not supplied" in {
        Form(TransportDetails.formMapping)
          .bind(Map[String, String]("container" -> ""))
          .fold(_ must haveErrorMessage("Please give an answer"), _ => fail("should not succeed"))
      }

      "invalid paymentMethod is supplied" in {

        forAll(arbitrary[TransportDetails], alphaNumStr) { (transportDetails, method) =>
          val data = transportDetails.copy(paymentMethod = Some(method))
          Form(TransportDetails.formMapping)
            .fillAndValidate(data)
            .fold(
              _ must haveErrorMessage("Input is not valid"),
              _ => fail("should not succeed")
            )
        }
      }

    }
  }
}
