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

package views.declaration.summary.sections

import base.Injector
import models.declaration.submissions.EnhancedStatus.{CLEARED, GOODS_ARRIVED, GOODS_HAVE_EXITED}
import models.declaration.submissions.{Submission, SubmissionAction}
import play.api.libs.json.Json
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec
import views.declaration.summary.sections.NotificationSectionViewSpec._
import views.helpers.EnhancedStatusHelper.asText
import views.helpers.ViewDates
import views.html.declaration.summary.sections.notifications_section

import java.time.ZonedDateTime

class NotificationSectionViewSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  val section = instanceOf[notifications_section]

  val view = section(Some(submission))(messages)

  "Notification Section view" should {

    "contain the expected MRN and notification rows" in {
      view.getElementsByClass("govuk-summary-list__row").size mustBe 4

      val row0 = view.getElementsByClass("mrn-row")
      row0 must haveSummaryKey(messages("declaration.summary.accepted.mrn"))
      row0 must haveSummaryValue(mrn)

      val expectedListOfDateTime: Seq[ZonedDateTime] =
        submission.actions.filter {
          case _: SubmissionAction => true
          case _                   => false
        }.flatMap(_.notifications.map(_.map(_.dateTimeIssued))).head

      val row1 = view.getElementsByClass("goods_arrived-row")
      row1 must haveSummaryKey(asText(GOODS_ARRIVED))
      row1 must haveSummaryValue(ViewDates.formatDateAtTime(expectedListOfDateTime(0)))

      val row2 = view.getElementsByClass("cleared-row")
      row2 must haveSummaryKey(asText(CLEARED))
      row2 must haveSummaryValue(ViewDates.formatDateAtTime(expectedListOfDateTime(1)))

      val row3 = view.getElementsByClass("goods_have_exited-row")
      row3 must haveSummaryKey(asText(GOODS_HAVE_EXITED))
      row3 must haveSummaryValue(ViewDates.formatDateAtTime(expectedListOfDateTime(2)))
    }

    "NOT contain any notification row" in {
      val view = section(Some(submissionWithoutNotificationSummaries))(messages)
      view.getElementsByClass("govuk-summary-list__row").size mustBe 1

      val row0 = view.getElementsByClass("mrn-row")
      row0 must haveSummaryKey(messages("declaration.summary.accepted.mrn"))
      row0 must haveSummaryValue(mrn)
    }
  }
}

object NotificationSectionViewSpec {

  val mrn = "18GBJ4L5DKXCVUUNZZ"

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
      |            "versionNo" : 2,
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
      |            "decId" : "id"
      |        }
      |    ],
      |    "enhancedStatusLastUpdated" : "2022-07-06T08:15:22Z[UTC]",
      |    "latestEnhancedStatus" : "GOODS_ARRIVED",
      |    "mrn" : "$mrn",
      |    "latestDecId" : "TEST-N3fwz-PAwaKfh4",
      |    "latestVersionNo" : 1,
      |    "blockAmendments" : false
      |}
      |""".stripMargin)
    .as[Submission]

  val submissionWithoutNotificationSummaries = Json
    .parse("""{
      |    "uuid" : "TEST-N3fwz-PAwaKfh4",
      |    "eori" : "IT165709468566000",
      |    "lrn" : "MBxIq",
      |    "ducr" : "7SI755462446188-51Z8126",
      |    "actions" : [
      |        {
      |            "id" : "abdf6423-b7fd-4f40-b325-c34bdcdfb203",
      |            "requestType" : "CancellationRequest",
      |            "requestTimestamp" : "2022-07-06T08:05:20.477Z[UTC]",
      |            "decId" : "id",
      |            "versionNo" : 2
      |        },
      |        {
      |            "id" : "dddf6423-b7fd-4f40-b325-c34bdcdfb204",
      |            "requestType" : "SubmissionRequest",
      |            "requestTimestamp" : "2022-07-06T08:05:20.477Z[UTC]",
      |            "notifications" : [],
      |            "decId" : "id"
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
}
