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

package connectors

import java.time.LocalDateTime
import java.util.UUID

import base.{Injector, MockHttpClient, TestHelper}
import config.AppConfig
import connectors.CustomsDeclareExportsConnector.toXml
import forms.CancelDeclaration
import models.declaration.notifications.Notification
import models.declaration.submissions.RequestType.SubmissionRequest
import models.declaration.submissions.{Action, Submission}
import models.requests.CancellationRequested
import org.scalatest.concurrent.ScalaFutures
import play.api.http.{ContentTypes, HeaderNames}
import play.api.libs.ws.WSClient
import play.api.mvc.Codec
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization
import unit.base.UnitSpec

import scala.concurrent.ExecutionContext.global

class CustomsDeclareExportsConnectorSpec extends UnitSpec with ScalaFutures with Injector {
  import CustomsDeclareExportsConnectorSpec._

  val appConfig = instanceOf[AppConfig]
  val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(TestHelper.createRandomString(255))))

  val mockWSClient = mock[WSClient]

  "Customs Declare Exports Connector" should {

    "GET to Customs Declare Exports endpoint to fetch notifications" in {
      val http =
        new MockHttpClient(mockWSClient, expectedExportsUrl(appConfig.fetchNotifications), None, result = notifications)
      val client = new CustomsDeclareExportsConnector(appConfig, http)
      val response = client.fetchNotifications()(hc, global)

      response.futureValue must be(notifications)
    }

    "GET to Customs Declare Exports endpoint to fetch submissions" in {
      val http =
        new MockHttpClient(mockWSClient, expectedExportsUrl(appConfig.fetchSubmissions), None, result = submissions)
      val client = new CustomsDeclareExportsConnector(appConfig, http)
      val response = client.fetchSubmissions()(hc, global)

      response.futureValue must be(submissions)
    }

    "POST to Customs Declare Exports endpoint to submit cancellation" in {
      val metadata = cancellationRequest.createCancellationMetadata("eori")

      val http = new MockHttpClient(
        mockWSClient,
        expectedExportsUrl(appConfig.cancelDeclaration),
        toXml(metadata),
        cancellationHeaders,
        falseServerError,
        CancellationRequested
      )
      val client = new CustomsDeclareExportsConnector(appConfig, http)
      val response = client.submitCancellation(mrn, metadata)(hc, global)

      response.futureValue must be(CancellationRequested)
    }
  }

  private def expectedExportsUrl(endpointUrl: String): String = s"${appConfig.customsDeclareExports}${endpointUrl}"
}

object CustomsDeclareExportsConnectorSpec {
  val mrn = TestHelper.createRandomAlphanumericString(10)

  val conversationId = TestHelper.createRandomAlphanumericString(10)

  val exportNotification =
    Notification(conversationId, mrn, LocalDateTime.now, "01", None, Seq.empty, "payload")

  val notifications = Seq(exportNotification)

  val submissionData = Submission(
    uuid = UUID.randomUUID().toString,
    eori = "eori",
    lrn = "lrn",
    mrn = None,
    ducr = None,
    actions = Seq(
      Action(requestType = SubmissionRequest, conversationId = "conversationID", requestTimestamp = LocalDateTime.now())
    )
  )

  val submissions = Seq(submissionData)

  val cancellationHeaders: Seq[(String, String)] = Seq(
    HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8),
    HeaderNames.ACCEPT -> ContentTypes.XML(Codec.utf_8),
    "X-MRN" -> mrn
  )
  val falseServerError: Boolean = false

  val cancellationRequest =
    CancelDeclaration(functionalReferenceId = "", declarationId = "", statementDescription = "", changeReason = "")
}
