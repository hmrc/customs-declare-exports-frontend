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

import connectors.{CustomsDeclarationsConnector, CustomsDeclareExportsConnector, CustomsInventoryLinkingExportsConnector, NrsConnector}
import models._
import models.requests.CancellationStatus
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers.{ACCEPTED, BAD_REQUEST, OK}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

trait MockConnectors extends MockitoSugar {
  lazy val mockCustomsDeclarationsConnector: CustomsDeclarationsConnector = mock[CustomsDeclarationsConnector]
  lazy val mockCustomsDeclareExportsConnector: CustomsDeclareExportsConnector = mock[CustomsDeclareExportsConnector]
  lazy val mockCustomsInventoryLinkingExportsConnector: CustomsInventoryLinkingExportsConnector =
    mock[CustomsInventoryLinkingExportsConnector]
  lazy val mockNrsConnector: NrsConnector = mock[NrsConnector]

  def successfulCustomsDeclarationResponse() = {
    when(mockCustomsDeclarationsConnector.submitExportDeclaration(any(), any())(any(), any()))
      .thenReturn(Future.successful(CustomsDeclarationsResponse(ACCEPTED, Some("1234"))))

    when(mockCustomsDeclarationsConnector.submitCancellation(any(), any())(any(), any()))
      .thenReturn(Future.successful(CustomsDeclarationsResponse(ACCEPTED, Some("1234"))))

    when(mockCustomsDeclareExportsConnector.saveSubmissionResponse(any())(any(), any()))
      .thenReturn(Future.successful(CustomsDeclareExportsResponse(OK, "message")))
  }

  def customsDeclaration400Response() = {
    when(mockCustomsDeclarationsConnector.submitExportDeclaration(any(), any())(any(), any()))
      .thenReturn(Future.successful(CustomsDeclarationsResponse(BAD_REQUEST, None)))

    when(mockCustomsDeclarationsConnector.submitCancellation(any(), any())(any(), any()))
      .thenReturn(Future.successful(CustomsDeclarationsResponse(BAD_REQUEST, None)))
  }

  def listOfNotifications() =
    when(mockCustomsDeclareExportsConnector.fetchNotifications()(any(), any()))
      .thenReturn(
        Future.successful(Seq(ExportsNotification(DateTime.now(), "", "", None, DeclarationMetadata(), Seq.empty)))
      )

  def listOfSubmissionNotifications() =
    when(mockCustomsDeclareExportsConnector.fetchNotificationsByConversationId(any())(any(), any()))
      .thenReturn(
        Future.successful(
          Some(ExportsNotification(conversationId = "1234", eori = "eori", metadata = DeclarationMetadata()))
        )
      )

  def listOfSubmissions() =
    when(mockCustomsDeclareExportsConnector.fetchSubmissions()(any(), any()))
      .thenReturn(
        Future.successful(
          Seq(
            SubmissionData(
              eori = "eori",
              conversationId = "conversationId",
              ducr = "ducr",
              mrn = None,
              lrn = None,
              submittedTimestamp = System.currentTimeMillis(),
              status = None,
              noOfNotifications = 0
            )
          )
        )
      )

  def sendMovementRequest() =
    when(mockCustomsInventoryLinkingExportsConnector.sendMovementRequest(any(), any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(ACCEPTED)))

  def sendMovementRequest400Response() =
    when(mockCustomsInventoryLinkingExportsConnector.sendMovementRequest(any(), any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(BAD_REQUEST)))

  def submitNrsRequest() =
    when(mockNrsConnector.submitNonRepudiation(any())(any(), any()))
      .thenReturn(Future.successful(NrsSubmissionResponse("submissionId1")))

  def successfulCancelDeclarationResponse(status: CancellationStatus) =
    when(mockCustomsDeclareExportsConnector.cancelDeclaration(any())(any(), any()))
      .thenReturn(Future.successful(status))
}
