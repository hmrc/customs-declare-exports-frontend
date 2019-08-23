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

import controllers.declaration.{routes, CarrierDetailsController}
import forms.Choice
import forms.declaration.{CarrierDetails, EntityDetails}
import play.api.libs.json.Json
import play.api.test.Helpers._
import unit.base.ControllerSpec
import views.html.declaration.carrier_details

class CarrierDetailsControllerSpec extends ControllerSpec {

  trait SetUp {
    val carrierDetailsPage = new carrier_details(mainTemplate)

    val controller = new CarrierDetailsController(
      mockAuthAction,
      mockJourneyAction,
      mockExportsCacheService,
      navigator,
      stubMessagesControllerComponents(),
      carrierDetailsPage
    )(ec)

    authorizedUser()
    withNewCaching(aDeclaration(withChoice(Choice.AllowedChoiceValues.SupplementaryDec)))
  }

  "Carrier Details Controller" should {

    "return OK (200)" when {

      "display page method is invoked and cache is empty" in new SetUp {

        val result = controller.displayForm()(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked and cache contains data" in new SetUp {

        withNewCaching(aDeclaration(withCarrierDetails(Some("1234"))))

        val result = controller.displayForm()(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in new SetUp {

        val incorrectForm = Json.toJson(CarrierDetails(EntityDetails(None, None)))

        val result = controller.saveAddress()(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "information provided by user are correct" in new SetUp {

        val correctForm = Json.toJson(CarrierDetails(EntityDetails(Some("12345678"), None)))

        val result = controller.saveAddress()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.DeclarationAdditionalActorsController.displayForm()
      }
    }
  }
}
