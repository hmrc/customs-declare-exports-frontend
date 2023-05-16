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

package base

import base.ExportsTestData.{aDeclaration, withStatus}
import connectors.CustomsDeclareExportsConnector
import models._
import models.declaration.DeclarationStatus
import models.declaration.notifications.Notification
import models.declaration.submissions.{Action, Submission}
import org.mockito.ArgumentMatchers.{any, anyString, refEq}
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.{Answer, OngoingStubbing}
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

// TODO This mock should extends BeforeAndAfterEach trait and has methods beforeEach and afterEach
trait MockConnectors {
  lazy val mockCustomsDeclareExportsConnector: CustomsDeclareExportsConnector = mock[CustomsDeclareExportsConnector]

  def successfulCustomsDeclareExportsResponse(): Unit =
    when(mockCustomsDeclareExportsConnector.createDeclaration(any[ExportsDeclaration])(any(), any()))
      .thenAnswer(withTheFirstArgument)

  private def withTheFirstArgument[T]: Answer[Future[T]] = (invocation: InvocationOnMock) => Future.successful(invocation.getArgument(0))

  def customsDeclaration400Response(): Unit =
    when(mockCustomsDeclareExportsConnector.createDeclaration(any())(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.failed(new IllegalArgumentException("Bad Request")))

  def listOfDraftDeclarations(): OngoingStubbing[Future[Paginated[ExportsDeclaration]]] =
    when(mockCustomsDeclareExportsConnector.findSavedDeclarations(any[Page])(any(), any()))
      .thenReturn(Future.successful(Paginated(draftDeclarations, Page(), 1)))

  private def draftDeclarations: Seq[ExportsDeclaration] = Seq(aDeclaration(withStatus(DeclarationStatus.DRAFT)))

  def deleteDraftDeclaration(): OngoingStubbing[Future[Unit]] =
    when(mockCustomsDeclareExportsConnector.deleteDraftDeclaration(anyString())(any(), any())).thenReturn(Future.successful((): Unit))

  def fetchAction(action: Action): OngoingStubbing[Future[Option[Action]]] =
    when(mockCustomsDeclareExportsConnector.findAction(refEq(action.id))(any(), any())).thenReturn(Future.successful(Some(action)))

  def fetchDeclaration(id: String): OngoingStubbing[Future[Option[ExportsDeclaration]]] =
    when(mockCustomsDeclareExportsConnector.findDeclaration(refEq(id))(any(), any())).thenReturn(Future.successful(Some(aDeclaration())))

  def fetchLatestNotification(notification: Notification): OngoingStubbing[Future[Option[Notification]]] =
    when(mockCustomsDeclareExportsConnector.findLatestNotification(refEq(notification.actionId))(any(), any()))
      .thenReturn(Future.successful(Some(notification)))

  def fetchDeclaration(id: String, declaration: ExportsDeclaration): OngoingStubbing[Future[Option[ExportsDeclaration]]] =
    when(mockCustomsDeclareExportsConnector.findDeclaration(refEq(id))(any(), any())).thenReturn(Future.successful(Some(declaration)))

  def declarationNotFound: OngoingStubbing[Future[Option[ExportsDeclaration]]] =
    when(mockCustomsDeclareExportsConnector.findDeclaration(anyString())(any(), any())).thenReturn(Future.successful(None))

  def cancelDeclarationResponse(response: CancellationStatus = CancellationRequestSent): OngoingStubbing[Future[CancellationStatus]] =
    when(mockCustomsDeclareExportsConnector.createCancellation(any())(any(), any())).thenReturn(Future.successful(response))

  def fetchSubmission(id: String, submission: Submission): OngoingStubbing[Future[Option[Submission]]] =
    when(mockCustomsDeclareExportsConnector.findSubmission(refEq(id))(any(), any())).thenReturn(Future.successful(Some(submission)))

  def submissionNotFound: OngoingStubbing[Future[Option[Submission]]] =
    when(mockCustomsDeclareExportsConnector.findSubmission(anyString())(any(), any())).thenReturn(Future.successful(None))

  def findNotifications(id: String, notifications: Seq[Notification] = Seq.empty): OngoingStubbing[Future[Seq[Notification]]] =
    when(mockCustomsDeclareExportsConnector.findNotifications(refEq(id))(any(), any())).thenReturn(Future.successful(notifications))
}
