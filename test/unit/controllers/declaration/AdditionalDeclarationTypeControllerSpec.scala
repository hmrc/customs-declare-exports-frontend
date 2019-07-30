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

import controllers.declaration.AdditionalDeclarationTypeController
import forms.Choice
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import play.api.libs.json.Json
import play.api.test.Helpers._
import unit.base.ControllerSpec
import views.html.declaration.additionaldeclarationtype.declaration_type

class AdditionalDeclarationTypeControllerSpec extends ControllerSpec {

  trait SetUp {
    val additionalDeclarationTypePage = new declaration_type(mainTemplate)

    val controller = new AdditionalDeclarationTypeController(
      mockAuthAction,
      mockJourneyAction,
      mockCustomsCacheService,
      mockExportsCacheService,
      stubMessagesControllerComponents(),
      additionalDeclarationTypePage
    )(minimalAppConfig, ec)

    authorizedUser()
    withCaching(None)
  }

  trait SupplementarySetUp extends SetUp {
    withNewCaching(aCacheModel(withChoice(Choice.AllowedChoiceValues.SupplementaryDec)))
  }

  trait StandardSetUp extends SetUp {
    withNewCaching(aCacheModel(withChoice(Choice.AllowedChoiceValues.StandardDec)))
  }

  "Additional declaration type page controller for supplementary" should {

    "return 200 (OK)" when {

      "display page method is invoked without data in cache" in new SupplementarySetUp {

        val result = controller.displayPage()(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked with data in cache" in new SupplementarySetUp {

        val cachedData = aCacheModel(withChoice(Choice.AllowedChoiceValues.SupplementaryDec))
          .copy(additionalDeclarationType = Some(AdditionalDeclarationType("Z")))
        withNewCaching(cachedData)

        val result = controller.displayPage()(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in new SupplementarySetUp {

        val incorrectForm = Json.toJson(AdditionalDeclarationType("Incorrect"))

        val result = controller.submitForm()(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER) and redirect to consignment references page" when {

      "data is correct" in new SupplementarySetUp {

        val correctForm = Json.toJson(AdditionalDeclarationType("Z"))

        val result = controller.submitForm()(postRequest(correctForm))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/consignment-references"))
      }
    }
  }

  "Additional declaration type page controller for standard" should {

    "return 200 (OK)" when {

      "display page method is invoked without data in cache" in new StandardSetUp {

        val result = controller.displayPage()(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked with data in cache" in new StandardSetUp {

        val cachedData = aCacheModel(withChoice(Choice.AllowedChoiceValues.SupplementaryDec))
          .copy(additionalDeclarationType = Some(AdditionalDeclarationType("D")))
        withNewCaching(cachedData)

        val result = controller.displayPage()(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in new StandardSetUp {

        val incorrectForm = Json.toJson(AdditionalDeclarationType("Incorrect"))

        val result = controller.submitForm()(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER) and redirect to consignment references page" when {

      "data is correct" in new StandardSetUp {

        val correctForm = Json.toJson(AdditionalDeclarationType("D"))

        val result = controller.submitForm()(postRequest(correctForm))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/consignment-references"))
      }
    }
  }
}
