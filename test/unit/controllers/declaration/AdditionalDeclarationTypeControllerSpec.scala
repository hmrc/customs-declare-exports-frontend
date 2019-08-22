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
import controllers.util.{SaveAndContinue, SaveAndReturn}
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
      mockExportsCacheService,
      navigator,
      stubMessagesControllerComponents(),
      additionalDeclarationTypePage
    )(ec)

    authorizedUser()
  }

  trait SupplementarySetUp extends SetUp {
    withNewCaching(aDeclaration(withChoice(Choice.AllowedChoiceValues.SupplementaryDec)))
  }

  trait StandardSetUp extends SetUp {
    withNewCaching(aDeclaration(withChoice(Choice.AllowedChoiceValues.StandardDec)))
  }

  "Display Page" should {
    "return 200 (OK)" when {
      "cache is empty during supplementary journey" in new SupplementarySetUp {
        val result = controller.displayPage()(getRequest())

        status(result) must be(OK)
      }

      "cache is populated during supplementary journey" in new SupplementarySetUp {
        val cachedData = aDeclaration(withChoice(Choice.AllowedChoiceValues.SupplementaryDec))
          .copy(additionalDeclarationType = Some(AdditionalDeclarationType("Z")))
        withNewCaching(cachedData)

        val result = controller.displayPage()(getRequest())

        status(result) must be(OK)
      }

      "cache is empty during standard journey" in new StandardSetUp {
        val result = controller.displayPage()(getRequest())

        status(result) must be(OK)
      }

      "cache is populated during standard journey" in new StandardSetUp {
        val cachedData = aDeclaration(withChoice(Choice.AllowedChoiceValues.SupplementaryDec))
          .copy(additionalDeclarationType = Some(AdditionalDeclarationType("D")))
        withNewCaching(cachedData)

        val result = controller.displayPage()(getRequest())

        status(result) must be(OK)
      }
    }
  }

  "Submit" should {
    "return 400 (BAD_REQUEST)" when {
      "form is incorrect during supplementary journey" in new SupplementarySetUp {
        val incorrectForm = Json.toJson(AdditionalDeclarationType("Incorrect"))

        val result = controller.submitForm()(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }

      "form is incorrect during standard journey" in new StandardSetUp {
        val incorrectForm = Json.toJson(AdditionalDeclarationType("Incorrect"))

        val result = controller.submitForm()(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER) and redirect to consignment references page" when {
      "SaveAndContinue during supplementary journey" in new SupplementarySetUp {
        val correctForm = Seq("additionalDeclarationType" -> "Z", SaveAndContinue.toString -> "")

        val result = controller.submitForm()(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(
          Some(controllers.declaration.routes.ConsignmentReferencesController.displayPage().url)
        )
      }

      "SaveAndContinue during standard journey" in new StandardSetUp {
        val correctForm = Seq("additionalDeclarationType" -> "D", SaveAndContinue.toString -> "")

        val result = controller.submitForm()(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(
          Some(controllers.declaration.routes.ConsignmentReferencesController.displayPage().url)
        )
      }
    }

    "return 303 (SEE_OTHER) and redirect to draft confirmation page" when {
      "SaveAndReturn during supplementary journey" in new SupplementarySetUp {
        val correctForm = Seq("additionalDeclarationType" -> "Z", SaveAndReturn.toString -> "")

        val result = controller.submitForm()(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe draftConfirmationResult
      }

      "SaveAndReturn during standard journey" in new StandardSetUp {
        val correctForm = Seq("additionalDeclarationType" -> "D", SaveAndReturn.toString -> "")

        val result = controller.submitForm()(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe draftConfirmationResult
      }
    }
  }
}
