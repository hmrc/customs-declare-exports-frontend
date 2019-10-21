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

import controllers.declaration.BorderTransportController
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.TransportCodes.{cash, IMOShipIDNumber}
import forms.declaration.BorderTransport
import models.{DeclarationType, Mode}
import play.api.libs.json.Json
import play.api.test.Helpers._
import unit.base.ControllerSpec
import views.html.declaration.border_transport

class BorderTransportControllerSpec extends ControllerSpec {

  trait SetUp {
    val borderTransportPage = new border_transport(mainTemplate)

    val controller = new BorderTransportController(
      mockAuthAction,
      mockJourneyAction,
      navigator,
      mockExportsCacheService,
      stubMessagesControllerComponents(),
      borderTransportPage
    )(ec)

    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))
  }

  "Transport Details Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in new SetUp {

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked and cache is not empty" in new SetUp {

        withNewCaching(aDeclaration(withTransportDetails()))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form contains incorrect values" in new SetUp {

        val incorrectForm = Json.toJson(BorderTransport(Some("incorrect"), false, "", "", None))

        val result = controller.submitForm(Mode.Normal)(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {
      "Container is selected" in new SetUp {
        val correctForm =
          Json.toJson(BorderTransport(Some("United Kingdom"), true, IMOShipIDNumber, "correct", Some(cash)))

        val result = controller.submitForm(Mode.Draft)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.TransportContainerController
          .displayContainerSummary(Mode.Draft)
      }

      "Container is not selected" in new SetUp {
        val correctForm =
          Json.toJson(BorderTransport(Some("United Kingdom"), false, IMOShipIDNumber, "correct", Some(cash)))

        val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.SummaryController.displayPage(Mode.Normal)
      }

      "Container is not selected in draft mode" in new SetUp {
        val correctForm =
          Json.toJson(BorderTransport(Some("United Kingdom"), false, IMOShipIDNumber, "correct", Some(cash)))

        val result = controller.submitForm(Mode.Draft)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.SummaryController.displayPage(Mode.Normal)
      }
    }
  }
}
