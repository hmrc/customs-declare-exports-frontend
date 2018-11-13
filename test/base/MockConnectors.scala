/*
 * Copyright 2018 HM Revenue & Customs
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

import connectors.{CustomsDeclarationsConnector, CustomsDeclareExportsConnector, CustomsInventoryLinkingExportsConnector}
import models._
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

  def successfulCustomsDeclarationResponse() = {
    when(mockCustomsDeclarationsConnector.submitExportDeclaration(any(), any())(any(), any()))
      .thenReturn(Future.successful(CustomsDeclarationsResponse(ACCEPTED,Some("1234"))))

    when(mockCustomsDeclarationsConnector.submitCancellation(any(), any())(any(), any()))
      .thenReturn(Future.successful(CustomsDeclarationsResponse(ACCEPTED,Some("1234"))))
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
    when(mockCustomsDeclareExportsConnector.fetchNotifications(any())(any(), any()))
      .thenReturn(Future.successful(Seq(ExportsNotification(DateTime.now(), "", "", None, DeclarationMetadata(), Seq.empty))))

  def sendArrival() =
    when(mockCustomsInventoryLinkingExportsConnector.sendArrival(any(), any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(ACCEPTED)))

  def sendArrival400Response() =
    when(mockCustomsInventoryLinkingExportsConnector.sendArrival(any(), any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(BAD_REQUEST)))
}
