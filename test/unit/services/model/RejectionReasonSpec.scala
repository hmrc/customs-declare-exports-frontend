/*
 * Copyright 2020 HM Revenue & Customs
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

import models.Pointer
import models.declaration.notifications.{Notification, NotificationError}
import models.declaration.submissions.SubmissionStatus
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito.given
import play.api.i18n.Messages
import services.model.RejectionReason
import unit.base.UnitSpec

class RejectionReasonSpec extends UnitSpec {

  import services.model.RejectionReason._
  private val messages = mock[Messages]

  "Apply" should {

    "create correct error based on the list" in {

      val errorCode = "ErrorCode"
      val cdsErrorDescription = "Error description"
      val exportsErrorDescription = "Improved Error description"
      val error = List(errorCode, cdsErrorDescription, exportsErrorDescription)

      RejectionReason.apply(error) mustBe RejectionReason(errorCode, cdsErrorDescription, exportsErrorDescription, None)
    }

    "throw an exception when input is incorrect" in {

      intercept[IllegalArgumentException](RejectionReason.apply(List.empty))
    }
  }

  "All Errors" should {
    "have 136 errors" in {

      allRejectionErrors.length mustBe 136
    }

    "contain correct values" in {

      allRejectionErrors must contain(RejectionReason("CDS40049", "Quota exhausted.", "Quota exhausted", None))
      allRejectionErrors must contain(RejectionReason("CDS40051", "Quota blocked.", "Quota blocked", None))
      allRejectionErrors must contain(
        RejectionReason(
          "CDS12087",
          "Relation error: VAT Declaring Party Identification (D.E. 3/40), where mandated, must be supplied at either header or item.",
          "Relation error: VAT Declaring Party Identification (D.E. 3/40), where mandated, must be supplied at either header or item",
          None
        )
      )
      allRejectionErrors must contain(
        RejectionReason("CDS12108", "Obligation error: DUCR is mandatory on an Export Declaration.", "An export declaration needs a DUCR", None)
      )
    }

    "correctly read multiline values" in {

      val expectedMessage =
        """Sequence error: The referred declaration does not comply with one of the following conditions:
          |- The AdditionalMessage.declarationReference must refer to an existing declaration (Declaration.reference),
          |- have been accepted,
          |- not be invalidated.""".stripMargin
      val expectedRejectionReason =
        RejectionReason("CDS12015", expectedMessage, "Declaration does not exist or is not ready to process the request", None)

      allRejectionErrors must contain(expectedRejectionReason)
    }
  }

  "Map from Notifications" should {

    "map to Rejected Reason" when {
      val acceptedNotification =
        Notification("convId", "mrn", LocalDateTime.now(), SubmissionStatus.ACCEPTED, Seq.empty, "")

      "list is empty" in {
        fromNotifications(Seq.empty)(messages) mustBe Seq.empty
      }

      "list doesn't contain rejected notification" in {
        fromNotifications(Seq(acceptedNotification))(messages) mustBe Seq.empty
      }

      "list contains rejected notification" when {

        "pointer is known" in {
          given(messages.isDefinedAt("field.x.$.z")).willReturn(true)
          val error = NotificationError("CDS12016", Some(Pointer("x.#0.z")))
          val notification =
            Notification("actionId", "mrn", LocalDateTime.now(), SubmissionStatus.REJECTED, Seq(error), "")

          fromNotifications(Seq(notification))(messages) mustBe Seq(
            RejectionReason(
              "CDS12016",
              "Date error: Date of acceptance is not allowed.",
              "The acceptance date cannot be more than 180 days in the past",
              Some(Pointer("x.#0.z"))
            )
          )
        }

        "pointer is unknown" in {
          given(messages.isDefinedAt(anyString())).willReturn(false)
          val error = NotificationError("CDS12016", Some(Pointer("x.#0.z")))
          val notification =
            Notification("actionId", "mrn", LocalDateTime.now(), SubmissionStatus.REJECTED, Seq(error), "")

          fromNotifications(Seq(notification))(messages) mustBe Seq(
            RejectionReason(
              "CDS12016",
              "Date error: Date of acceptance is not allowed.",
              "The acceptance date cannot be more than 180 days in the past",
              None
            )
          )
        }

        "pointer is empty" in {
          val error = NotificationError("CDS12016", None)
          val notification =
            Notification("actionId", "mrn", LocalDateTime.now(), SubmissionStatus.REJECTED, Seq(error), "")

          fromNotifications(Seq(notification))(messages) mustBe Seq(
            RejectionReason(
              "CDS12016",
              "Date error: Date of acceptance is not allowed.",
              "The acceptance date cannot be more than 180 days in the past",
              None
            )
          )
        }
      }
    }
  }
}
