/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.CancelDeclarationDescription
import forms.cancellation.CancellationChangeReason.NoLongerRequired
import metrics.{ExportsMetrics, MetricIdentifiers}
import mock.ExportsMetricsMocks
import models.CancellationStatus.CancellationResult
import models.declaration.ExportDeclarationTestData.declaration
import models.requests.SessionHelper._
import models.{CancelDeclaration, CancellationAlreadyRequested}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{any, eq => equalTo}
import org.mockito.Mockito.{verify, when}
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import services.audit.{AuditService, AuditTypes}
import testdata.SubmissionsTestData.submission
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import uk.gov.hmrc.play.bootstrap.metrics.Metrics
import views.html.cancel_declaration

import scala.concurrent.Future

class CancelDeclarationControllerSpec extends ControllerWithoutFormSpec with ExportsMetricsMocks with Injector {
  import CancelDeclarationControllerSpec._

  trait SetUp {
    val mockAuditService = mock[AuditService]
    val cancelDeclarationPage = instanceOf[cancel_declaration]

    val additionalDeclarationType = "D"
    val sessionData = Map(submissionUuid -> "submissionUuid", submissionLrn -> "lrn", submissionMrn -> "mrn", submissionDucr -> "ducr")

    val controller = new CancelDeclarationController(
      mockAuthAction,
      mockVerifiedEmailAction,
      mockErrorHandler,
      mockCustomsDeclareExportsConnector,
      mockExportsMetrics,
      mcc,
      mockAuditService,
      cancelDeclarationPage
    )(ec)

    setupErrorHandler()
    authorizedUser()

    when(mockExportsMetrics.startTimer(any())).thenReturn(mock[Timer.Context])
    when(mockAuditService.auditAllPagesDeclarationCancellation(any())(any())).thenReturn(Future.successful(Success))
    when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any())).thenReturn(Future.successful(Some(declaration)))
    when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any())).thenReturn(Future.successful(Some(submission)))
  }

  "Cancel declaration controller" should {

    "return 200 (OK)" when {

      "display page method is invoked" in new SetUp {
        val result = controller.displayPage(getRequestWithSession(sessionData.toSeq: _*))
        status(result) must be(OK)
      }

      "cancellation is requested with duplicate request error" in new SetUp {
        cancelDeclarationResponse(CancellationResult(CancellationAlreadyRequested, Some("conversationId")))

        val result = controller.onSubmit(postRequestWithSession(correctCancelDeclarationJSON, sessionData.toSeq))
        status(result) must be(OK)

        verify(mockAuditService).auditAllPagesDeclarationCancellation(any())(any())
      }
    }

    "call audit service with expected auditTypes" in new SetUp {
      cancelDeclarationResponse()
      val result = controller.onSubmit(postRequestWithSession(correctCancelDeclarationJSON, sessionData.toSeq))
      status(result) must be(SEE_OTHER)

      verify(mockAuditService).auditAllPagesDeclarationCancellation(any())(any())
      verify(mockAuditService).audit(equalTo(AuditTypes.Cancellation), any())(any())
    }

    "return a 303 redirect to holding page" when {
      "cancellation is requested with success" in new SetUp {
        cancelDeclarationResponse()
        val result = controller.onSubmit(postRequestWithSession(correctCancelDeclarationJSON, sessionData.toSeq))
        status(result) must be(SEE_OTHER)
        redirectLocation(result) mustBe Some(routes.CancellationResultController.displayHoldingPage.url)
      }
    }

    "return 400 BadRequest error page" when {
      "session data is missing" when {

        "submissionUuid" in new SetUp {
          val session = (sessionData - submissionUuid).toSeq

          val getResult = controller.displayPage(getRequestWithSession(session: _*))
          val postResult = controller.onSubmit(postRequestWithSession(correctCancelDeclarationJSON, session))

          status(getResult) must be(BAD_REQUEST)
          contentAsString(postResult) mustBe empty

          status(postResult) must be(BAD_REQUEST)
          contentAsString(postResult) mustBe empty
        }

        "lrn" in new SetUp {
          val session = (sessionData - submissionLrn).toSeq

          val getResult = controller.displayPage(getRequestWithSession(session: _*))
          val postResult = controller.onSubmit(postRequestWithSession(correctCancelDeclarationJSON, session))

          status(postResult) must be(BAD_REQUEST)
          contentAsString(postResult) mustBe empty

          status(getResult) must be(BAD_REQUEST)
          contentAsString(postResult) mustBe empty
        }

        "mrn" in new SetUp {
          val session = (sessionData - submissionMrn).toSeq

          val getResult = controller.displayPage(getRequestWithSession(session: _*))
          val postResult = controller.onSubmit(postRequestWithSession(correctCancelDeclarationJSON, session))

          status(postResult) must be(BAD_REQUEST)
          contentAsString(postResult) mustBe empty

          status(getResult) must be(BAD_REQUEST)
          contentAsString(postResult) mustBe empty
        }

        "ducr" in new SetUp {
          val session = (sessionData - submissionDucr).toSeq

          val getResult = controller.displayPage(getRequestWithSession(session: _*))
          val postResult = controller.onSubmit(postRequestWithSession(correctCancelDeclarationJSON, session))

          status(postResult) must be(BAD_REQUEST)
          contentAsString(postResult) mustBe empty

          status(getResult) must be(BAD_REQUEST)
          contentAsString(postResult) mustBe empty
        }
      }
    }
  }

  "Cancel Declaration Controller on POST" should {

    "record cancellation timing and increase the Success Counter when response is OK" in new SetUp {
      authorizedUser()

      val exportMetrics = instanceOf[ExportsMetrics]

      val registry = instanceOf[Metrics].defaultRegistry
      val cancelMetric = MetricIdentifiers.cancellationMetric

      val cancelTimer = registry.getTimers().get(exportMetrics.timerName(cancelMetric))
      val cancelCounter = registry.getCounters().get(exportMetrics.counterName(cancelMetric))

      val timerBefore = cancelTimer.getCount
      val counterBefore = cancelCounter.getCount

      successfulCustomsDeclareExportsResponse()
      cancelDeclarationResponse()

      val result = controller.onSubmit(postRequestWithSession(correctCancelDeclarationJSON, sessionData.toSeq))

      status(result) must be(SEE_OTHER)

      cancelTimer.getCount mustBe >=(timerBefore)
      cancelCounter.getCount mustBe >=(counterBefore)
      verify(mockAuditService).auditAllPagesDeclarationCancellation(any())(any())
      verify(mockAuditService).audit(ArgumentMatchers.eq(AuditTypes.Cancellation), any())(any())
    }

    "propagate errors from exports connector" in new SetUp {
      val error = new RuntimeException("some error")
      when(mockCustomsDeclareExportsConnector.createCancellation(any[CancelDeclaration])(any(), any())).thenThrow(error)

      val result = controller.onSubmit(postRequestWithSession(correctCancelDeclarationJSON, sessionData.toSeq))

      status(result) mustBe INTERNAL_SERVER_ERROR
    }
  }
}

object CancelDeclarationControllerSpec {
  val correctCancelDeclaration =
    CancelDeclarationDescription(statementDescription = "Some description", changeReason = NoLongerRequired.toString)

  val correctCancelDeclarationJSON: JsValue = Json.toJson(correctCancelDeclaration)

  val incorrectCancelDeclaration =
    CancelDeclarationDescription("description", "wrong reason")

  val incorrectCancelDeclarationJSON: JsValue = Json.toJson(incorrectCancelDeclaration)
}
