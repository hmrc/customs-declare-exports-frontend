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

import base.Injector
import com.codahale.metrics.Timer
import com.kenshoo.play.metrics.Metrics
import controllers.CancelDeclarationController
import forms.CancelDeclaration
import forms.cancellation.CancellationChangeReason.NoLongerRequired
import metrics.{ExportsMetrics, MetricIdentifiers}
import models.requests.{CancellationRequestExists, CancellationRequested, MissingDeclaration}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import unit.base.ControllerSpec
import unit.mock.{ErrorHandlerMocks, ExportsMetricsMocks}
import views.html.{cancel_declaration, cancellation_confirmation_page}

class CancelDeclarationControllerSpec
    extends ControllerSpec with ErrorHandlerMocks with ExportsMetricsMocks with Injector {
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
    )(ec)

    setupErrorHandler()
    authorizedUser()

    when(mockExportsMetrics.startTimer(any())).thenReturn(mock[Timer.Context])
  }

  "Cancel declaration controller" should {

    "return 200 (OK)" when {

      "display page method is invoked" in new SetUp {

        val result = controller.displayPage()(getRequest())

        status(result) must be(OK)
      }

      "cancellation is requested with success" in new SetUp {

        successfulCancelDeclarationResponse()

        val result = controller.onSubmit()(postRequest(correctCancelDeclarationJSON))

        status(result) must be(OK)
      }
    }
  }

  "Cancel Declaration Controller on POST" should {

    "record cancellation timing and increase the Success Counter when response is OK" in new SetUp {
      authorizedUser()

      val exportMetrics = instanceOf[ExportsMetrics]

      val registry = instanceOf[Metrics].defaultRegistry
      val cancelMetric = MetricIdentifiers.cancelMetric

      val cancelTimer = registry.getTimers().get(exportMetrics.timerName(cancelMetric))
      val cancelCounter = registry.getCounters().get(exportMetrics.counterName(cancelMetric))

      val timerBefore = cancelTimer.getCount
      val counterBefore = cancelCounter.getCount

      successfulCustomsDeclareExportsResponse()
      successfulCancelDeclarationResponse()

      val result = controller.onSubmit()(postRequest(correctCancelDeclarationJSON))

      status(result) must be(OK)

      val stringResult = contentAsString(result)
      stringResult must include("cancellation.confirmationPage.message")
      cancelTimer.getCount mustBe >=(timerBefore)
      cancelCounter.getCount mustBe >=(counterBefore)
    }
  }
}

object CancelDeclarationControllerSpec {
  val correctCancelDeclaration =
    CancelDeclaration(
      functionalReferenceId = "1SA123456789012-1FSA1234567",
      mrn = "87654321",
      statementDescription = "Some description",
      changeReason = NoLongerRequired.toString
    )

  val correctCancelDeclarationJSON: JsValue = Json.toJson(correctCancelDeclaration)

  val incorrectCancelDeclaration = CancelDeclaration("functionalRefernceId", "decId", "description", "wrong reason")

  val incorrectCancelDeclarationJSON: JsValue = Json.toJson(incorrectCancelDeclaration)
}
