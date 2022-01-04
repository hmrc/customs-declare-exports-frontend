/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers

import base.{ControllerWithoutFormSpec, Injector}
import com.codahale.metrics.Timer
import com.kenshoo.play.metrics.Metrics
import forms.cancellation.CancellationChangeReason.NoLongerRequired
import forms.{CancelDeclaration, Lrn}
import metrics.{ExportsMetrics, MetricIdentifiers}
import mock.{ErrorHandlerMocks, ExportsMetricsMocks}
import models.{CancellationAlreadyRequested, MrnNotFound}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import services.audit.{AuditService, AuditTypes}
import views.html.{cancel_declaration, cancellation_confirmation_page}

class CancelDeclarationControllerSpec extends ControllerWithoutFormSpec with ErrorHandlerMocks with ExportsMetricsMocks with Injector {
  import CancelDeclarationControllerSpec._

  trait SetUp {
    val mockAuditService = mock[AuditService]
    val cancelDeclarationPage = instanceOf[cancel_declaration]
    val cancelConfirmationPage = instanceOf[cancellation_confirmation_page]

    val controller = new CancelDeclarationController(
      mockAuthAction,
      mockVerifiedEmailAction,
      mockCustomsDeclareExportsConnector,
      mockExportsMetrics,
      stubMessagesControllerComponents(),
      mockAuditService,
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
        cancelDeclarationResponse()

        val result = controller.onSubmit()(postRequest(correctCancelDeclarationJSON))
        status(result) must be(OK)
      }

      "cancellation is requested with MRN not found error" in new SetUp {
        cancelDeclarationResponse(MrnNotFound)

        val result = controller.onSubmit()(postRequest(correctCancelDeclarationJSON))
        status(result) must be(OK)
      }

      "cancellation is requested with duplicate request error" in new SetUp {
        cancelDeclarationResponse(CancellationAlreadyRequested)

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
      cancelDeclarationResponse()

      val result = controller.onSubmit()(postRequest(correctCancelDeclarationJSON))

      status(result) must be(OK)

      val stringResult = contentAsString(result)
      stringResult must include("cancellation.confirmationPage.message")
      cancelTimer.getCount mustBe >=(timerBefore)
      cancelCounter.getCount mustBe >=(counterBefore)
      verify(mockAuditService).auditAllPagesDeclarationCancellation(any())(any())
      verify(mockAuditService).audit(ArgumentMatchers.eq(AuditTypes.Cancellation), any())(any())
    }

    "propagate errors from exports connector" in new SetUp {
      val error = new RuntimeException("some error")
      when(mockCustomsDeclareExportsConnector.createCancellation(any[CancelDeclaration])(any(), any())).thenThrow(error)

      val result = controller.onSubmit()(postRequest(correctCancelDeclarationJSON))

      intercept[Exception](status(result)) mustBe error
    }
  }
}

object CancelDeclarationControllerSpec {
  val correctCancelDeclaration =
    CancelDeclaration(
      functionalReferenceId = Lrn("1SA123456789012"),
      mrn = "123456789012345678",
      statementDescription = "Some description",
      changeReason = NoLongerRequired.toString
    )

  val correctCancelDeclarationJSON: JsValue = Json.toJson(correctCancelDeclaration)

  val incorrectCancelDeclaration =
    CancelDeclaration(Lrn("functionalRefernceId"), "decId", "description", "wrong reason")

  val incorrectCancelDeclarationJSON: JsValue = Json.toJson(incorrectCancelDeclaration)
}
