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

import com.codahale.metrics.Timer
import controllers.CancelDeclarationController
import forms.CancelDeclaration
import forms.cancellation.CancellationChangeReason.NoLongerRequired
import models.requests.{CancellationRequestExists, CancellationRequested, MissingDeclaration}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import unit.base.ControllerSpec
import unit.mock.{ErrorHandlerMocks, ExportsMetricsMocks}
import views.html.{cancel_declaration, cancellation_confirmation_page}

class CancelDeclarationControllerSpec extends ControllerSpec with ErrorHandlerMocks with ExportsMetricsMocks {
  import CancelDeclarationControllerSpec._

  trait SetUp {
    val cancelDeclarationPage = new cancel_declaration(mainTemplate)
    val cancelConfirmationPage = new cancellation_confirmation_page(mainTemplate)

    val controller = new CancelDeclarationController(
      mockAuthAction,
      mockCustomsDeclareExportsConnector,
      mockErrorHandler,
      mockExportsMetrics,
      stubMessagesControllerComponents(),
      cancelDeclarationPage,
      cancelConfirmationPage
    )(ec, minimalAppConfig)

    setupErrorHandler()
    authorizedUser()

    when(mockExportsMetrics.startTimer(any())).thenReturn(mock[Timer.Context])
  }

  "Cancel declaration controller" should {

    "return 200 (OK)" when {

      "display page method is invoked" in new SetUp {

        val result = controller.displayForm()(getRequest())

        status(result) must be(OK)
      }

      "cancellation is requested with success" in new SetUp {

        successfulCancelDeclarationResponse(CancellationRequested)

        val result = controller.onSubmit()(postRequest(correctCancelDeclarationJSON))

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "cancellation request already exists" in new SetUp {

        successfulCancelDeclarationResponse(CancellationRequestExists)

        val result = controller.onSubmit()(postRequest(correctCancelDeclarationJSON))

        status(result) must be(BAD_REQUEST)
      }

      "declaration is missing" in new SetUp {

        successfulCancelDeclarationResponse(MissingDeclaration)

        val result = controller.onSubmit()(postRequest(correctCancelDeclarationJSON))

        status(result) must be(BAD_REQUEST)
      }

      "form is incorrect" in new SetUp {

        val result = controller.onSubmit()(postRequest(incorrectCancelDeclarationJSON))

        status(result) must be(BAD_REQUEST)
      }
    }
  }
}

object CancelDeclarationControllerSpec {
  val correctCancelDeclaration =
    CancelDeclaration(
      functionalReferenceId = "1SA123456789012-1FSA1234567",
      declarationId = "87654321",
      statementDescription = "Some description",
      changeReason = NoLongerRequired.toString
    )

  val correctCancelDeclarationJSON: JsValue = Json.toJson(correctCancelDeclaration)

  val incorrectCancelDeclaration = CancelDeclaration("functionalRefernceId", "decId", "description", "wrong reason")

  val incorrectCancelDeclarationJSON: JsValue = Json.toJson(incorrectCancelDeclaration)
}
