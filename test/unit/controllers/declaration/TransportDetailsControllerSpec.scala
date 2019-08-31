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

import controllers.declaration.TransportDetailsController
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.TransportCodes.{cash, IMOShipIDNumber}
import forms.declaration.TransportDetails
import models.Mode
import play.api.libs.json.Json
import play.api.test.Helpers._
import unit.base.ControllerSpec
import views.html.declaration.transport_details

class TransportDetailsControllerSpec extends ControllerSpec {

  trait SetUp {
    val transportDetailsPage = new transport_details(mainTemplate)

    val controller = new TransportDetailsController(
      mockAuthAction,
      mockJourneyAction,
      navigator,
      mockExportsCacheService,
      stubMessagesControllerComponents(),
      transportDetailsPage
    )(ec)

    authorizedUser()
    withNewCaching(aDeclaration(withChoice(SupplementaryDec)))
  }

  "Transport Details Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in new SetUp {

        val result = controller.displayForm(Mode.Normal)(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked and cache is not empty" in new SetUp {

        withNewCaching(aDeclaration(withTransportDetails()))

        val result = controller.displayForm(Mode.Normal)(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form contains incorrect values" in new SetUp {

        val incorrectForm = Json.toJson(TransportDetails(Some("incorrect"), false, "", None, None))

        val result = controller.submitForm(Mode.Normal)(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" in new SetUp {

      val correctForm =
        Json.toJson(TransportDetails(Some("United Kingdom"), true, IMOShipIDNumber, Some("correct"), Some(cash)))

      val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

      await(result) mustBe aRedirectToTheNextPage
      thePageNavigatedTo mustBe controllers.declaration.routes.TransportContainerController.displayAddContainer()
    }
  }
}
