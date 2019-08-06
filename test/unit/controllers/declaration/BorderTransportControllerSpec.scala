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

import controllers.declaration.{routes, BorderTransportController}
import forms.Choice
import forms.declaration.BorderTransport
import forms.declaration.TransportCodes.{Maritime, WagonNumber}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers._
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.html.declaration.border_transport

import scala.concurrent.Future

class BorderTransportControllerSpec extends ControllerSpec with ErrorHandlerMocks {

  trait SetUp {
    val borderTransportPage = new border_transport(mainTemplate)

    val controller = new BorderTransportController(
      mockAuthAction,
      mockJourneyAction,
      mockExportsCacheService,
      stubMessagesControllerComponents(),
      borderTransportPage
    )(ec)

    setupErrorHandler()
    authorizedUser()
    withNewCaching(aCacheModel(withChoice(Choice.AllowedChoiceValues.SupplementaryDec)))
  }

  "Border transport controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in new SetUp {

        val result: Future[Result] = controller.displayForm()(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked and cache contains data" in new SetUp {
        withNewCaching(
          aCacheModel(
            withChoice(Choice.AllowedChoiceValues.SupplementaryDec),
            withBorderTransport(Maritime, WagonNumber, None)
          )
        )

        val result: Future[Result] = controller.displayForm()(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in new SetUp {

        val incorrectForm: JsValue = Json.toJson(BorderTransport("wrongValue", "wrongValue", None))

        val result: Future[Result] = controller.submitForm()(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "information provided by user are correct" in new SetUp {

        val correctForm: JsValue = Json.toJson(BorderTransport(Maritime, WagonNumber, None))

        val result: Future[Result] = controller.submitForm()(postRequest(correctForm))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(routes.TransportDetailsController.displayForm().url))
      }
    }
  }
}
