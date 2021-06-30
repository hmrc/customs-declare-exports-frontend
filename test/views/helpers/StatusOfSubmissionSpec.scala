/*
 * Copyright 2021 HM Revenue & Customs
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

import models.declaration.submissions.SubmissionStatus._
import views.declaration.spec.UnitViewSpec

class StatusOfSubmissionSpec extends UnitViewSpec {

  "StatusOfSubmission" should {

    "correctly convert the SubmissionStatus id to the expected text" in {

      StatusOfSubmission.asText(PENDING) mustBe "Pending"
      StatusOfSubmission.asText(REQUESTED_CANCELLATION) mustBe "Cancellation Requested"
      StatusOfSubmission.asText(ACCEPTED) mustBe "Accepted"
      StatusOfSubmission.asText(RECEIVED) mustBe "Received"
      StatusOfSubmission.asText(REJECTED) mustBe "Rejected"
      StatusOfSubmission.asText(UNDERGOING_PHYSICAL_CHECK) mustBe "Undergoing Physical Check"
      StatusOfSubmission.asText(ADDITIONAL_DOCUMENTS_REQUIRED) mustBe "Documents Required"
      StatusOfSubmission.asText(AMENDED) mustBe "Amended"
      StatusOfSubmission.asText(RELEASED) mustBe "Released"
      StatusOfSubmission.asText(CLEARED) mustBe "Cleared"
      StatusOfSubmission.asText(CANCELLED) mustBe "Cancelled"
      StatusOfSubmission.asText(CUSTOMS_POSITION_GRANTED) mustBe "Customs Position Granted"
      StatusOfSubmission.asText(CUSTOMS_POSITION_DENIED) mustBe "Customs Position Denied"
      StatusOfSubmission.asText(GOODS_HAVE_EXITED_THE_COMMUNITY) mustBe "Goods Have Exited The Community"
      StatusOfSubmission.asText(DECLARATION_HANDLED_EXTERNALLY) mustBe "Declaration Handled Externally"
      StatusOfSubmission.asText(AWAITING_EXIT_RESULTS) mustBe "Awaiting Exit Results"
      StatusOfSubmission.asText(QUERY_NOTIFICATION_MESSAGE) mustBe "Query Raised"
      StatusOfSubmission.asText(UNKNOWN) mustBe "Unknown status"
    }
  }
}
