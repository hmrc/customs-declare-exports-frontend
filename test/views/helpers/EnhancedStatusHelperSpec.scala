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

package views.helpers

import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.RequestType.{AmendmentCancellationRequest, AmendmentRequest, SubmissionRequest}
import models.declaration.submissions.{NotificationSummary, RequestType, Submission}
import play.api.libs.json.Json
import views.helpers.EnhancedStatusHelper.{asText, asTimelineEvent}
import views.helpers.EnhancedStatusHelperSpec.{submission, submissionWithDMSQRY}
import views.common.UnitViewSpec

import java.time.ZonedDateTime
import java.util.UUID

class EnhancedStatusHelperSpec extends UnitViewSpec {

  "EnhancedStatusHelper" should {

    "correctly convert the EnhancedStatus id to the expected text" in {
      asText(ADDITIONAL_DOCUMENTS_REQUIRED) mustBe "Documents required"
      asText(AMENDED) mustBe "Amended"
      asText(ON_HOLD) mustBe "On hold"
      asText(AWAITING_EXIT_RESULTS) mustBe "Awaiting exit results"
      asText(CANCELLED) mustBe "Cancelled"
      asText(CLEARED) mustBe "Declaration cleared"
      asText(CUSTOMS_POSITION_DENIED) mustBe "Customs position denied"
      asText(CUSTOMS_POSITION_GRANTED) mustBe "Customs position granted"
      asText(DECLARATION_HANDLED_EXTERNALLY) mustBe "Declaration handled externally"
      asText(ERRORS) mustBe "Declaration rejected"
      asText(EXPIRED_NO_ARRIVAL) mustBe "Declaration expired (no arrival)"
      asText(EXPIRED_NO_DEPARTURE) mustBe "Declaration expired (no departure)"
      asText(GOODS_ARRIVED) mustBe "Arrived and accepted"
      asText(GOODS_ARRIVED_MESSAGE) mustBe "Arrived and accepted"
      asText(GOODS_HAVE_EXITED) mustBe "Goods have exited the UK"
      asText(QUERY_NOTIFICATION_MESSAGE) mustBe "Query raised"
      asText(RECEIVED) mustBe "Declaration submitted"
      asText(RELEASED) mustBe "Released"
      asText(UNDERGOING_PHYSICAL_CHECK) mustBe "Goods being examined"
      asText(WITHDRAWN) mustBe "Withdrawn"
      asText(PENDING) mustBe "Pending"
      asText(REQUESTED_CANCELLATION) mustBe "Cancellation request submitted"
      asText(UNKNOWN) mustBe "Unknown"

      val event = (requestType: RequestType, status: EnhancedStatus) =>
        NotificationEvent("someId", requestType, NotificationSummary(UUID.randomUUID(), ZonedDateTime.now(), status))

      asTimelineEvent(event(AmendmentRequest, CUSTOMS_POSITION_GRANTED)) mustBe "Amendment accepted"
      asTimelineEvent(event(AmendmentRequest, CUSTOMS_POSITION_DENIED)) mustBe "Amendment failed"
      asTimelineEvent(event(AmendmentRequest, ERRORS)) mustBe "Amendment rejected"
      asTimelineEvent(event(AmendmentCancellationRequest, CUSTOMS_POSITION_GRANTED)) mustBe "Amendment cancelled"
      asTimelineEvent(event(SubmissionRequest, CUSTOMS_POSITION_DENIED)) mustBe "Cancellation request denied"
      asTimelineEvent(event(SubmissionRequest, CUSTOMS_POSITION_GRANTED)) mustBe "Customs position granted"
    }

    "confirm NO DMSQRY notification has been received for a Submission" in {
      hasQueryNotificationMessageStatus(submission) mustBe false
    }

    "confirm a DMSQRY notification has been received for a Submission" in {
      hasQueryNotificationMessageStatus(submissionWithDMSQRY) mustBe true
    }

    def hasQueryNotificationMessageStatus(submission: Submission): Boolean =
      submission.actions.exists { action =>
        action.requestType == SubmissionRequest && action.notifications.exists(_.exists(_.enhancedStatus == QUERY_NOTIFICATION_MESSAGE))
      }
  }
}

object EnhancedStatusHelperSpec {

  val submission = Json
    .parse(s"""{
      |    "uuid" : "TEST-N3fwz-PAwaKfh4",
      |    "eori" : "IT165709468566000",
      |    "lrn" : "MBxIq",
      |    "ducr" : "7SI755462446188-51Z8126",
      |    "actions" : [
      |        {
      |            "id" : "abdf6423-b7fd-4f40-b325-c34bdcdfb203",
      |            "requestType" : "CancellationRequest",
      |            "requestTimestamp" : "2022-07-06T08:05:20.477Z[UTC]",
      |            "versionNo" : 1,
      |            "decId" : "id"
      |        },
      |        {
      |            "id" : "dddf6423-b7fd-4f40-b325-c34bdcdfb204",
      |            "requestType" : "SubmissionRequest",
      |            "requestTimestamp" : "2022-06-04T09:08:20.800Z[UTC]",
      |            "notifications" : [
      |                {
      |                    "notificationId" : "149a4470-f29c-4e33-8a75-b9a119a50c06",
      |                    "dateTimeIssued" : "2022-06-04T08:15:22Z[UTC]",
      |                    "enhancedStatus" : "GOODS_ARRIVED"
      |                },
      |                {
      |                    "notificationId" : "149a4470-f29c-4e33-8a75-b9a119a50c06",
      |                    "dateTimeIssued" : "2022-06-04T08:10:22Z[UTC]",
      |                    "enhancedStatus" : "CLEARED"
      |                },
      |                {
      |                    "notificationId" : "149a4470-f29c-4e33-8a75-b9a119a50c06",
      |                    "dateTimeIssued" : "2022-06-04T08:05:22Z[UTC]",
      |                    "enhancedStatus" : "GOODS_HAVE_EXITED"
      |                }
      |            ],
      |            "decId" : "id",
      |            "versionNo" : 1
      |        }
      |    ],
      |    "enhancedStatusLastUpdated" : "2022-07-06T08:15:22Z[UTC]",
      |    "latestEnhancedStatus" : "GOODS_ARRIVED",
      |    "mrn" : "18GBJ4L5DKXCVUUNZZ",
      |    "latestDecId" : "TEST-N3fwz-PAwaKfh4",
      |    "latestVersionNo" : 1,
      |    "blockAmendments" : false
      |}
      |""".stripMargin)
    .as[Submission]

  val submissionWithDMSQRY = Json
    .parse(s"""{
      |    "uuid" : "TEST-N3fwz-PAwaKfh4",
      |    "eori" : "IT165709468566000",
      |    "lrn" : "MBxIq",
      |    "ducr" : "7SI755462446188-51Z8126",
      |    "actions" : [
      |        {
      |            "id" : "dddf6423-b7fd-4f40-b325-c34bdcdfb204",
      |            "requestType" : "SubmissionRequest",
      |            "requestTimestamp" : "2022-06-04T09:08:20.800Z[UTC]",
      |            "notifications" : [
      |                {
      |                    "notificationId" : "149a4470-f29c-4e33-8a75-b9a119a50c06",
      |                    "dateTimeIssued" : "2022-06-04T08:15:22Z[UTC]",
      |                    "enhancedStatus" : "QUERY_NOTIFICATION_MESSAGE"
      |                }
      |            ],
      |            "decId" : "id",
      |            "versionNo" : 1
      |        }
      |    ],
      |    "enhancedStatusLastUpdated" : "2022-07-06T08:15:22Z[UTC]",
      |    "latestEnhancedStatus" : "QUERY_NOTIFICATION_MESSAGE",
      |    "mrn" : "18GBJ4L5DKXCVUUNZZ",
      |    "latestDecId" : "TEST-N3fwz-PAwaKfh4",
      |    "latestVersionNo" : 1,
      |    "blockAmendments" : false
      |}
      |""".stripMargin)
    .as[Submission]
}
