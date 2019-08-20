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
import models.DeclarationStatus.DeclarationStatus
import models._
import models.declaration.notifications.Notification
import models.declaration.submissions.{Action, Submission}
import models.declaration.submissions.RequestType.SubmissionRequest
import models.requests.CancellationStatus
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.{Answer, OngoingStubbing}
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockConnectors extends MockitoSugar {
  lazy val mockCustomsDeclareExportsConnector: CustomsDeclareExportsConnector = mock[CustomsDeclareExportsConnector]

  lazy val mockNrsConnector: NrsConnector = mock[NrsConnector]

  def successfulCustomsDeclareExportsResponse(): Unit =
    when(mockCustomsDeclareExportsConnector.createDeclaration(any[ExportsDeclaration])(any(), any()))
      .thenAnswer(withTheFirstArgument)

  private def withTheFirstArgument[T]: Answer[Future[T]] = new Answer[Future[T]] {
    override def answer(invocation: InvocationOnMock): Future[T] = Future.successful(invocation.getArgument(0))
  }

  def customsDeclaration400Response(): Unit =
    when(mockCustomsDeclareExportsConnector.createDeclaration(any())(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.failed(new IllegalArgumentException("Bad Request")))

  def listOfNotifications(): OngoingStubbing[Future[Seq[Notification]]] =
    when(mockCustomsDeclareExportsConnector.fetchNotifications()(any(), any()))
      .thenReturn(
        Future.successful(Seq(Notification("convId", "mrn", LocalDateTime.now(), "01", None, Seq.empty, "payload")))
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

  def listOfDraftDeclarations(): OngoingStubbing[Future[Paginated[ExportsDeclaration]]] =
    when(mockCustomsDeclareExportsConnector.findSavedDeclarations(any[Page])(any(), any()))
      .thenReturn(Future.successful(Paginated(draftDeclarations, Page(), 1)))

  def getDeclaration(id: String): OngoingStubbing[Future[Option[ExportsDeclaration]]] =
    when(mockCustomsDeclareExportsConnector.findDeclaration(refEq(id))(any(), any()))
      .thenReturn(Future.successful(Some(ExportsTestData.aDeclaration())))

  private def draftDeclarations: Seq[ExportsDeclaration] =
    Seq(ExportsTestData.aDeclaration(ExportsTestData.withStatus(DeclarationStatus.DRAFT)))

  def submitNrsRequest(): OngoingStubbing[Future[NrsSubmissionResponse]] =
    when(mockNrsConnector.submitNonRepudiation(any())(any(), any()))
      .thenReturn(Future.successful(NrsSubmissionResponse("submissionId1")))

  def successfulCancelDeclarationResponse(status: CancellationStatus): OngoingStubbing[Future[CancellationStatus]] =
    when(mockCustomsDeclareExportsConnector.submitCancellation(any(), any())(any(), any()))
      .thenReturn(Future.successful(status))
}
