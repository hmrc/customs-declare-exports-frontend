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

package base

import java.time.LocalDateTime
import java.util.UUID

import connectors.{CustomsDeclareExportsConnector, NrsConnector}
import models._
import models.declaration.notifications.Notification
import models.declaration.submissions.{Action, Submission, SubmissionRequest}
import models.requests.CancellationStatus
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers.{ACCEPTED, BAD_REQUEST}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

trait MockConnectors extends MockitoSugar {
  lazy val mockCustomsDeclareExportsConnector: CustomsDeclareExportsConnector = mock[CustomsDeclareExportsConnector]

  lazy val mockNrsConnector: NrsConnector = mock[NrsConnector]

  def successfulCustomsDeclareExportsResponse(): OngoingStubbing[Future[HttpResponse]] =
    when(mockCustomsDeclareExportsConnector.submitExportDeclaration(any(), any(), any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(ACCEPTED)))

  def customsDeclaration400Response(): OngoingStubbing[Future[HttpResponse]] =
    when(mockCustomsDeclareExportsConnector.submitExportDeclaration(any(), any(), any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(BAD_REQUEST)))

  def listOfNotifications(): OngoingStubbing[Future[Seq[Notification]]] =
    when(mockCustomsDeclareExportsConnector.fetchNotifications()(any(), any()))
      .thenReturn(
        Future.successful(Seq(Notification("convId", "mrn", LocalDateTime.now(), "01", None, Seq.empty, "payload")))
      )

  def listOfSubmissionNotifications(): OngoingStubbing[Future[Seq[Notification]]] =
    when(mockCustomsDeclareExportsConnector.fetchNotificationsByMrn(any())(any(), any()))
      .thenReturn(
        Future.successful(
          Seq(
            Notification("convID", "mrn", LocalDateTime.now, "01", None, Seq.empty, "payload")
          )
        )
      )

  def listOfSubmissions(): OngoingStubbing[Future[Seq[Submission]]] =
    when(mockCustomsDeclareExportsConnector.fetchSubmissions()(any(), any()))
      .thenReturn(
        Future.successful(
          Seq(
            Submission(
              uuid = UUID.randomUUID().toString,
              eori = "eori",
              lrn = "lrn",
              mrn = None,
              ducr = None,
              actions = Seq(
                Action(
                  requestType = SubmissionRequest,
                  conversationId = "conversationID",
                  requestTimestamp = LocalDateTime.now()
                )
              )
            )
          )
        )
      )

  def submitNrsRequest(): OngoingStubbing[Future[NrsSubmissionResponse]] =
    when(mockNrsConnector.submitNonRepudiation(any())(any(), any()))
      .thenReturn(Future.successful(NrsSubmissionResponse("submissionId1")))

  def successfulCancelDeclarationResponse(status: CancellationStatus): OngoingStubbing[Future[CancellationStatus]] =
    when(mockCustomsDeclareExportsConnector.submitCancellation(any(), any())(any(), any()))
      .thenReturn(Future.successful(status))
}
