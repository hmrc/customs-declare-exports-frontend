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

package base

import connectors.CustomsDeclareExportsConnector
import models._
import models.declaration.notifications.Notification
import models.declaration.submissions.Submission
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.{Answer, OngoingStubbing}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

// TODO This mock should extends BeforeAndAfterEach trait and has methods beforeEach and afterEach
trait MockConnectors extends MockitoSugar {
  lazy val mockCustomsDeclareExportsConnector: CustomsDeclareExportsConnector = mock[CustomsDeclareExportsConnector]

  def successfulCustomsDeclareExportsResponse(): Unit =
    when(mockCustomsDeclareExportsConnector.createDeclaration(any[ExportsDeclaration])(any(), any()))
      .thenAnswer(withTheFirstArgument)

  private def withTheFirstArgument[T]: Answer[Future[T]] = new Answer[Future[T]] {
    override def answer(invocation: InvocationOnMock): Future[T] = Future.successful(invocation.getArgument(0))
  }

  def customsDeclaration400Response(): Unit =
    when(mockCustomsDeclareExportsConnector.createDeclaration(any())(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.failed(new IllegalArgumentException("Bad Request")))

  def listOfDraftDeclarations(): OngoingStubbing[Future[Paginated[ExportsDeclaration]]] =
    when(mockCustomsDeclareExportsConnector.findSavedDeclarations(any[Page])(any(), any()))
      .thenReturn(Future.successful(Paginated(draftDeclarations, Page(), 1)))

  private def draftDeclarations: Seq[ExportsDeclaration] =
    Seq(ExportsTestData.aDeclaration(ExportsTestData.withStatus(DeclarationStatus.DRAFT)))

  def deleteDraftDeclaration(): OngoingStubbing[Future[Unit]] =
    when(mockCustomsDeclareExportsConnector.deleteDraftDeclaration(anyString())(any(), any()))
      .thenReturn(Future.successful((): Unit))

  def getDeclaration(id: String): OngoingStubbing[Future[Option[ExportsDeclaration]]] =
    when(mockCustomsDeclareExportsConnector.findDeclaration(refEq(id))(any(), any()))
      .thenReturn(Future.successful(Some(ExportsTestData.aDeclaration())))

  def declarationNotFound: OngoingStubbing[Future[Option[ExportsDeclaration]]] =
    when(mockCustomsDeclareExportsConnector.findDeclaration(anyString())(any(), any()))
      .thenReturn(Future.successful(None))

  def cancelDeclarationResponse(response: CancellationStatus = CancellationRequestSent): OngoingStubbing[Future[CancellationStatus]] =
    when(mockCustomsDeclareExportsConnector.createCancellation(any())(any(), any()))
      .thenReturn(Future.successful(response))

  def findSubmission(id: String, submission: Option[Submission] = None): OngoingStubbing[Future[Option[Submission]]] =
    when(mockCustomsDeclareExportsConnector.findSubmission(refEq(id))(any(), any()))
      .thenReturn(Future.successful(submission))

  def findNotifications(id: String, notifications: Seq[Notification] = Seq.empty): OngoingStubbing[Future[Seq[Notification]]] =
    when(mockCustomsDeclareExportsConnector.findNotifications(refEq(id))(any(), any()))
      .thenReturn(Future.successful(notifications))
}
