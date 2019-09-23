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

package unit.services.model

import java.time.LocalDateTime

import models.declaration.notifications.{Notification, NotificationError}
import models.declaration.submissions.SubmissionStatus
import services.model.RejectionReason
import unit.base.UnitSpec

class RejectionReasonSpec extends UnitSpec {

  import services.model.RejectionReason._

  "Rejection reason model" should {

    "create correct error based on the list" in {

      val errorCode = "ErrorCode"
      val errorDescription = "Error description"
      val error = List(errorCode, errorDescription)

      RejectionReason.apply(error) mustBe RejectionReason(errorCode, errorDescription)
    }

    "throw an exception when input is incorrect" in {

      intercept[IllegalArgumentException](RejectionReason.apply(List.empty))
    }

    "have 136 errors" in {

      allRejectedErrors.length mustBe 136
    }

    "contain correct values" in {

      allRejectedErrors must contain(RejectionReason("CDS40049", "Quota exhausted."))
      allRejectedErrors must contain(RejectionReason("CDS40051", "Quota blocked."))
      allRejectedErrors must contain(
        RejectionReason(
          "CDS12087",
          "Relation error: VAT Declaring Party Identification (D.E. 3/40), where mandated, must be supplied at either header or item."
        )
      )
      allRejectedErrors must contain(
        RejectionReason("CDS12108", "Obligation error: DUCR is mandatory on an Export Declaration.")
      )
    }

    "correctly read multiline values" in {

      val expectedMessages =
        """Sequence error: The referred declaration does not comply with one of the following conditions:
          |- The AdditionalMessage.declarationReference must refer to an existing declaration (Declaration.reference),
          |- have been accepted,
          |- not be invalidated.""".stripMargin
      val expectedRejectionReason = RejectionReason("CDS12015", expectedMessages)

      allRejectedErrors must contain(expectedRejectionReason)
    }

    "correctly return error description" in {

      getErrorDescription("CDS12016") mustBe "Date error: Date of acceptance is not allowed."
    }

    "return Unknown error when error code is not in rejected errors" in {

      getErrorDescription("unknown code") mustBe "Unknown error"
    }

    "successfully convert list of notifications to list of rejection reasons" when {

      val nonRejectionNotification =
        Notification("convId", "mrn", LocalDateTime.now(), SubmissionStatus.ACCEPTED, Seq.empty, "")

      "list is empty" in {

        fromNotifications(Seq.empty) mustBe Seq.empty
      }

      "list doesn't contain rejected notification" in {

        fromNotifications(Seq(nonRejectionNotification)) mustBe Seq.empty
      }

      "list contains rejected notification" in {

        val firstError = NotificationError("CDS12016", Seq.empty)
        val secondError = NotificationError("CDS12022", Seq.empty)
        val notificationErrors = Seq(firstError, secondError)
        val rejectionNotification =
          Notification("actionId", "mrn", LocalDateTime.now(), SubmissionStatus.REJECTED, notificationErrors, "")
        val notifications = Seq(nonRejectionNotification, rejectionNotification)
        val firstExpectedRejectionReason = RejectionReason("CDS12016", "Date error: Date of acceptance is not allowed.")
        val secondExpectedRejectionReason =
          RejectionReason("CDS12022", "Relation error: The sequence number is larger than the total.")

        fromNotifications(notifications) mustBe Seq(firstExpectedRejectionReason, secondExpectedRejectionReason)
      }
    }
  }
}
