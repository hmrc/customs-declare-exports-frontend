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

package controllers.declaration

import base.CustomExportsBaseSpec
import forms.Choice
import forms.Choice.choiceId
import forms.declaration.CommodityMeasure.commodityFormId
import forms.declaration.{CommodityMeasure, PackageInformation}
import generators.Generators
import helpers.views.declaration.CommodityMeasureMessages
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.verify
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.InsufficientEnrolments

class CommodityMeasureControllerSpec
    extends CustomExportsBaseSpec with CommodityMeasureMessages with Generators with PropertyChecks with OptionValues {

  private val uri = uriWithContextPath("/declaration/commodity-measure")
  private val form = CommodityMeasure.form()

  "Commodity Measure Controller" should {

    "displayForm" should {

      "return UNAUTHORIZED" when {

        "user does not have EORI" in {
          userWithoutEori()
          withCaching[CommodityMeasure](None)
          withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

          val result = route(app, getRequest(uri)).value
          intercept[InsufficientEnrolments](status(result))

        }
      }

      "return OK" when {

        "user is signed in" in {
          authorizedUser()
          withCaching[CommodityMeasure](None, commodityFormId)
          withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
          val packages = arbitraryPackagingSeq.sample.getOrElse(Seq.empty)
          withCaching[Seq[PackageInformation]](Some(packages), "PackageInformation")
          val result = route(app, getRequest(uri)).value
          val stringResult = contentAsString(result)
          status(result) mustBe OK
          stringResult must include(messages(title))
          stringResult must include(messages(supplementaryUnits))
          stringResult must include(messages(supplementaryUnitsHint))
          stringResult must include(messages(netMass))
          stringResult must include(messages(grossMass))
        }
      }

      "show global error" when {

        "when no packages added and user tries to navigate to the screen" in {
          authorizedUser()
          withCaching[Seq[PackageInformation]](None, "PackageInformation")
          withCaching[CommodityMeasure](None, commodityFormId)
          withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
          val result = route(app, getRequest(uri)).value
          status(result) mustBe BAD_REQUEST
          contentAsString(result) must include("You must add package information to proceed")

        }
      }
    }

    ".onSubmit" should {

      "return UNAUTHORIZED" when {

        "user does not have an EORI" in {
          userWithoutEori()
          withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
          val body = Seq(("typesOfPackages", "A1"))
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).value

          intercept[InsufficientEnrolments](status(result))
        }
      }

      "return BAD_REQUEST" when {

        "invalid data is submitted" in {
          authorizedUser()
          withCaching[CommodityMeasure](None, commodityFormId)
          withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

          val body = Seq(("supplementaryUnits", "abcd"), ("netMass", ""), ("grossMass", ""))

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).value

          status(result) mustBe BAD_REQUEST
          val content = contentAsString(result)
          content must include(messages(netMassEmpty))
          content must include(messages(grossMassEmpty))
          content must include(messages(supplementaryUnitsError))

        }
      }

      "add Commodity Measure to the cache" when {

        "with valid data and on click of add" in {

          forAll(arbitrary[CommodityMeasure]) { commodityMeasure =>
            authorizedUser()
            withCaching[CommodityMeasure](None, commodityFormId)
            withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
            val body = Seq(
              ("supplementaryUnits", commodityMeasure.supplementaryUnits.getOrElse("")),
              ("netMass", commodityMeasure.netMass),
              ("grossMass", commodityMeasure.grossMass)
            )

            val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).value

            status(result) mustBe SEE_OTHER
            result.futureValue.header.headers.get("Location") must be(
              Some("/customs-declare-exports/declaration/additional-information")
            )

            verify(mockCustomsCacheService)
              .cache[CommodityMeasure](
                any(),
                ArgumentMatchers.eq(commodityFormId),
                ArgumentMatchers.eq(commodityMeasure)
              )(any(), any(), any())
          }
        }
      }

      "navigate to \"Additional Information\" page" when {

        "on click of continue when a record has already been added" in {
          forAll(arbitrary[CommodityMeasure]) { commodityMeasure =>
            authorizedUser()
            withCaching[CommodityMeasure](Some(commodityMeasure), commodityFormId)
            withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
            val payload = Seq(
              ("supplementaryUnits", commodityMeasure.supplementaryUnits.getOrElse("")),
              ("netMass", commodityMeasure.netMass),
              ("grossMass", commodityMeasure.grossMass)
            )
            val result = route(app, postRequestFormUrlEncoded(uri, payload: _*)).value
            status(result) mustBe SEE_OTHER
            result.futureValue.header.headers.get("Location") must be(
              Some("/customs-declare-exports/declaration/additional-information")
            )
          }
        }
      }
    }
  }
}
