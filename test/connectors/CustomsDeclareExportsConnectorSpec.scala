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

import base.TestHelper._
import base.{CustomExportsBaseSpec, MockHttpClient, TestHelper}
import models._
import models.declaration.notifications.Notification
import models.requests.CancellationRequested
import play.api.http.{ContentTypes, HeaderNames}
import play.api.mvc.Codec
import play.api.test.Helpers.ACCEPTED
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.wco.dec.MetaData

class CustomsDeclareExportsConnectorSpec extends CustomExportsBaseSpec {
  import CustomsDeclareExportsConnectorSpec._

  "Customs Declare Exports Connector" should {

    "POST to Customs Declare Exports endpoint to submit declaration" in {
      val http = new MockHttpClient(
        mockWSClient,
        expectedExportsUrl(appConfig.submitDeclaration),
        metadata.toXml,
        submissionHeaders,
        falseServerError,
        HttpResponse(ACCEPTED)
      )

      val client = new CustomsDeclareExportsConnector(appConfig, http)
      val response = client.submitExportDeclaration(Some(""), None, metadata.toXml)(hc, ec)

      response.futureValue.status must be(ACCEPTED)
    }

    "GET to Customs Declare Exports endpoint to fetch notifications" in {
      val http =
        new MockHttpClient(mockWSClient, expectedExportsUrl(appConfig.fetchNotifications), None, result = notifications)
      val client = new CustomsDeclareExportsConnector(appConfig, http)
      val response = client.fetchNotifications()(hc, ec)

      response.futureValue must be(notifications)
    }

    "GET to Customs Declare Exports endpoint to fetch notifications by conversationId" in {
      val http =
        new MockHttpClient(mockWSClient, expectedExportsUrl(appConfig.fetchNotifications), None, result = notifications)
      val client = new CustomsDeclareExportsConnector(appConfig, http)
      val response = client.fetchNotificationsByMrn(conversationId)(hc, ec)

      response.futureValue must be(notifications)
    }

    "GET to Customs Declare Exports endpoint to fetch submissions" in {
      val http =
        new MockHttpClient(mockWSClient, expectedExportsUrl(appConfig.fetchSubmissions), None, result = submissions)
      val client = new CustomsDeclareExportsConnector(appConfig, http)
      val response = client.fetchSubmissions()(hc, ec)

      response.futureValue must be(submissions)
    }

    "POST to Customs Declare Exports endpoint to submit cancellation" in {
      val http = new MockHttpClient(
        mockWSClient,
        expectedExportsUrl(appConfig.cancelDeclaration),
        metadata.toXml,
        cancellationHeaders,
        falseServerError,
        CancellationRequested
      )
      val client = new CustomsDeclareExportsConnector(appConfig, http)
      val response = client.submitCancellation(mrn, metadata)(hc, ec)

      response.futureValue must be(CancellationRequested)
    }
  }

  private def expectedExportsUrl(endpointUrl: String): String = s"${appConfig.customsDeclareExports}${endpointUrl}"

}

object CustomsDeclareExportsConnectorSpec {
  val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(createRandomAlphanumericString(255))))
  val mrn = TestHelper.createRandomAlphanumericString(10)
  val metadata = MetaData()

  val conversationId = TestHelper.createRandomAlphanumericString(10)
  val eori = TestHelper.createRandomAlphanumericString(15)
  val exportNotification =
    Notification(conversationId, mrn, LocalDateTime.now, "01", None, Seq.empty, "payload")
  val notifications = Seq(exportNotification)

  val submissionData = Submission(
    eori = eori,
    conversationId = conversationId,
    ducr = "",
    mrn = Some(mrn),
    lrn = None,
    submittedTimestamp = 20190318,
    status = Cancelled
  )
  val submissions = Seq(submissionData)

  val expectedHeaders: Seq[(String, String)] = Seq.empty
  val submissionHeaders: Seq[(String, String)] = Seq(
    (HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8)),
    (HeaderNames.ACCEPT -> ContentTypes.XML(Codec.utf_8)),
    ("X-DUCR", ""),
    ("X-LRN", "")
  )
  val cancellationHeaders: Seq[(String, String)] = Seq(
    (HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8)),
    (HeaderNames.ACCEPT -> ContentTypes.XML(Codec.utf_8)),
    ("X-MRN", mrn)
  )
  val falseServerError: Boolean = false

}
