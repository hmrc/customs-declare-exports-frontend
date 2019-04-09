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
      "has containers not supplied" in {
        Form(TransportDetails.formMapping)
          .bind(Map[String, String]("container" -> ""))
          .fold(_ must haveErrorMessage("Please give an answer"), _ => fail("should not succeed"))
      }
    }
  }
}
