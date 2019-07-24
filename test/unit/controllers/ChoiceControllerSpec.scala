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

package unit.controllers

import controllers.ChoiceController
import forms.Choice
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import unit.base.ControllerSpec
import views.html.choice_page

import scala.concurrent.Future

class ChoiceControllerSpec extends ControllerSpec {
  import ChoiceControllerSpec._

  trait SetUp {
    val choicePage = new choice_page(mainTemplate)

    val controller = new ChoiceController(
      mockAuthAction,
      mockCustomsCacheService,
      mockExportsCacheService,
      mockErrorHandler,
      stubMessagesControllerComponents(),
      choicePage
    )(ec, minimalAppConfig)

    authorizedUser()
    withCaching(None)
    withNewCaching(createModelWithNoItems(Choice.AllowedChoiceValues.SupplementaryDec))
  }

  "Choice controller" should {

    "return 200 (OK)" when {

      "display page method is invoked with empty cache" in new SetUp {
        when(mockExportsCacheService.get(any())).thenReturn(Future.successful(None))

        val result = controller.displayPage()(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked with data in cache" in new SetUp {
        withNewCaching(createModelWithItems("sessionId", Set.empty, Choice.AllowedChoiceValues.SupplementaryDec))

        val result = controller.displayPage()(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in new SetUp {

        val result = controller.submitChoice()(postRequest(incorrectChoice))

        status(result) must be(BAD_REQUEST)
      }
    }

    "redirect to Dispatch Location page" when {

      "user chose Supplementary Dec" in new SetUp {

        val result = controller.submitChoice()(postRequest(supplementaryChoice))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(
          Some(controllers.declaration.routes.DispatchLocationController.displayPage().url)
        )
      }

      "user chose Standard Dec" in new SetUp {

        val result = controller.submitChoice()(postRequest(standardChoice))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(
          Some(controllers.declaration.routes.DispatchLocationController.displayPage().url)
        )
      }
    }

    "redirect to Cancel Declaration page" when {

      "user chose Cancel Dec" in new SetUp {

        val result = controller.submitChoice()(postRequest(cancelChoice))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.routes.CancelDeclarationController.displayForm().url))
      }
    }

    "redirect to Submissions page" when {

      "user chose submissions" in new SetUp {

        val result = controller.submitChoice()(postRequest(submissionsChoice))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.routes.SubmissionsController.displayListOfSubmissions().url))
      }
    }
  }
}

object ChoiceControllerSpec {
  val incorrectChoice: JsValue = Json.toJson(Choice("Incorrect Choice"))
  val supplementaryChoice: JsValue = Json.toJson(Choice(Choice.AllowedChoiceValues.SupplementaryDec))
  val standardChoice: JsValue = Json.toJson(Choice(Choice.AllowedChoiceValues.StandardDec))
  val cancelChoice: JsValue = Json.toJson(Choice(Choice.AllowedChoiceValues.CancelDec))
  val submissionsChoice: JsValue = Json.toJson(Choice(Choice.AllowedChoiceValues.Submissions))
}
