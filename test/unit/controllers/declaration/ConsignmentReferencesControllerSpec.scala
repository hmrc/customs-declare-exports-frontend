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

package unit.controllers.declaration

import base.TestHelper
import controllers.declaration.ConsignmentReferencesController
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.Ducr
import forms.declaration.ConsignmentReferences
import play.api.libs.json.Json
import play.api.test.Helpers._
import unit.base.ControllerSpec
import views.html.declaration.consignment_references

class ConsignmentReferencesControllerSpec extends ControllerSpec {

  trait SetUp {
    val consignmentReferencesPage = new consignment_references(mainTemplate)

    val controller = new ConsignmentReferencesController(
      mockAuthAction,
      mockJourneyAction,
      mockExportsCacheService,
      stubMessagesControllerComponents(),
      consignmentReferencesPage
    )(ec)

    authorizedUser()
    withNewCaching(aDeclaration(withChoice(SupplementaryDec)))
  }

  "Consignment References controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in new SetUp {

        val result = controller.displayPage()(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked and cache contains data" in new SetUp {

        withNewCaching(aDeclaration(withConsignmentReferences()))

        val result = controller.displayPage()(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {
      "no UCR is submitted" in new SetUp {

        val incorrectForm = Json.toJson(ConsignmentReferences(Ducr("1234"), ""))

        val result = controller.submitConsignmentReferences()(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }

      "UCR length has been exceeded" in new SetUp {

        val incorrectForm =
          Json.toJson(ConsignmentReferences(Ducr("1234"), "", Some(TestHelper.createRandomAlphanumericString(36))))

        val result = controller.submitConsignmentReferences()(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }

      "UCR has special characters" in new SetUp {

        val incorrectForm =
          Json.toJson(ConsignmentReferences(Ducr("1234"), "", Some("special@&%")))

        val result = controller.submitConsignmentReferences()(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {
      "no UCR has been submitted" in new SetUp {

        val correctForm = Json.toJson(ConsignmentReferences(Ducr(DUCR), LRN))

        val result = controller.submitConsignmentReferences()(postRequest(correctForm))

        status(result) must be(SEE_OTHER)
      }

      "a correct UCR has been submitted" in new SetUp {

        val correctForm =
          Json.toJson(ConsignmentReferences(Ducr(DUCR), LRN, Some(TestHelper.createRandomAlphanumericString(35))))

        val result = controller.submitConsignmentReferences()(postRequest(correctForm))

        status(result) must be(SEE_OTHER)
      }
    }
  }
}
