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

import controllers.declaration.DispatchLocationController
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.DispatchLocation
import forms.declaration.DispatchLocation.AllowedDispatchLocations._
import play.api.libs.json.Json
import play.api.test.Helpers._
import unit.base.ControllerSpec
import views.html.declaration.dispatch_location

class DispatchLocationControllerSpec extends ControllerSpec {

  trait SetUp {
    val dispatchLocationPage = new dispatch_location(mainTemplate)

    val controller = new DispatchLocationController(
      mockAuthAction,
      mockJourneyAction,
      mockExportsCacheService,
      stubMessagesControllerComponents(),
      dispatchLocationPage
    )(ec)

    authorizedUser()
    withNewCaching(aDeclaration(withChoice(SupplementaryDec)))
  }

  "Dispatch Location controller" should {

    "return OK (200)" when {

      "display page method is invoked and cache is empty" in new SetUp {

        val result = controller.displayPage()(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked and cache contains data" in new SetUp {

        withNewCaching(aDeclaration(withDispatchLocation(OutsideEU)))

        val result = controller.displayPage()(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in new SetUp {

        val incorrectForm = Json.toJson(DispatchLocation("incorrect"))

        val result = controller.submitForm()(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER) and redirect to Additional Declaration Type page" when {

      "form is correct" in new SetUp {

        val correctForm = Json.toJson(DispatchLocation(OutsideEU))

        val result = controller.submitForm()(postRequest(correctForm))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/type"))
      }
    }

    "return 303 (SEE_OTHER) and redirect to Not Eligible page" when {

      "form is correct" in new SetUp {

        val correctForm = Json.toJson(DispatchLocation(SpecialFiscalTerritory))

        val result = controller.submitForm()(postRequest(correctForm))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/not-eligible"))
      }
    }
  }
}
