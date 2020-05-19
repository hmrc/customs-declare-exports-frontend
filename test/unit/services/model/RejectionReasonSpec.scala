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

import java.time.{Instant, ZoneOffset, ZonedDateTime}

import forms.declaration.Seal
import models.declaration.Container
import models.declaration.notifications.{Notification, NotificationError}
import models.declaration.submissions.SubmissionStatus
import models.{Pointer, PointerSection, PointerSectionType}
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito.given
import play.api.i18n.Messages
import services.cache.ExportsTestData
import services.model.RejectionReason
import unit.base.UnitSpec

class RejectionReasonSpec extends UnitSpec with ExportsTestData {

  import services.model.RejectionReason._
  private val messages = mock[Messages]

  "Apply" should {

    "create correct error based on the list" in {

      val errorCode = "ErrorCode"
      val cdsErrorDescription = "Error description"
      val exportsErrorDescription = "Improved Error description"
      val url = "/url"
      val error = List(errorCode, cdsErrorDescription, exportsErrorDescription, url)

      RejectionReason.apply(error) mustBe RejectionReason(errorCode, cdsErrorDescription, exportsErrorDescription, Some(url), None)
    }

    "create correct error from a single description" in {

      val errorCode = "ErrorCode"
      val cdsErrorDescription = "Error description"
      val error = List(errorCode, cdsErrorDescription, "", "")

      RejectionReason.apply(error) mustBe RejectionReason(errorCode, cdsErrorDescription, cdsErrorDescription, None, None)
    }

    "throw an exception when input is incorrect" in {

      intercept[IllegalArgumentException](RejectionReason.apply(List.empty))
    }
  }

  "All Errors" should {

    "have 199 errors" in {

      allRejectionReasons.length mustBe 199
    }

    "contain code for every error" in {

      allRejectionReasons.filter(_.code.isEmpty) mustBe empty
    }

    "contain cds description for every error" in {

      allRejectionReasons.filter(_.cdsDescription.isEmpty) mustBe empty
    }

    "contain exports description for every error" in {

      allRejectionReasons.filter(_.exportsDescription.isEmpty) mustBe empty
    }
  }

  "DMS error with code CDS40045" should {

    "have the correct url" in {

      allRejectionReasons.find(_.code == "CDS40045").flatMap(_.url) mustBe Some("/customs-declare-exports/declaration/items/ITEM_ID/add-document")
    }
  }

  "Map from Notifications" should {

    "map to Rejected Reason" when {
      val acceptedNotification =
        Notification("convId", "mrn", ZonedDateTime.now(ZoneOffset.UTC), SubmissionStatus.ACCEPTED, Seq.empty, "")

      "list is empty" in {
        fromNotifications(Seq.empty)(messages) mustBe Seq.empty
      }

      "list doesn't contain rejected notification" in {
        fromNotifications(Seq(acceptedNotification))(messages) mustBe Seq.empty
      }

      "list contains rejected notification" when {

        "pointer is known" in {
          given(messages.isDefinedAt("field.x.$.z")).willReturn(true)
          val error = NotificationError("CDS12016", Some(Pointer("x.#0.z")), None)
          val notification =
            Notification("actionId", "mrn", ZonedDateTime.now(ZoneOffset.UTC), SubmissionStatus.REJECTED, Seq(error), "")

          fromNotifications(Seq(notification))(messages) mustBe Seq(
            RejectionReason(
              "CDS12016",
              "Date Error: The acceptance date must not be in the future and must not be more than 180 days in the past",
              "The acceptance date cannot be more than 180 days in the past",
              None,
              Some(Pointer("x.#0.z"))
            )
          )
        }

        "pointer is unknown" in {
          given(messages.isDefinedAt(anyString())).willReturn(false)
          val error = NotificationError("CDS12016", Some(Pointer("x.#0.z")), None)
          val notification =
            Notification("actionId", "mrn", ZonedDateTime.now(ZoneOffset.UTC), SubmissionStatus.REJECTED, Seq(error), "")

          fromNotifications(Seq(notification))(messages) mustBe Seq(
            RejectionReason(
              "CDS12016",
              "Date Error: The acceptance date must not be in the future and must not be more than 180 days in the past",
              "The acceptance date cannot be more than 180 days in the past",
              None,
              None
            )
          )
        }

        "pointer is empty" in {
          val error = NotificationError("CDS12016", None, None)
          val notification =
            Notification("actionId", "mrn", ZonedDateTime.now(ZoneOffset.UTC), SubmissionStatus.REJECTED, Seq(error), "")

          fromNotifications(Seq(notification))(messages) mustBe Seq(
            RejectionReason(
              "CDS12016",
              "Date Error: The acceptance date must not be in the future and must not be more than 180 days in the past",
              "The acceptance date cannot be more than 180 days in the past",
              None,
              None
            )
          )
        }
      }
    }
  }

  "Rejection Reason" should {

    "correctly build url for item" in {

      val sections = Seq(
        PointerSection("declaration", PointerSectionType.FIELD),
        PointerSection("items", PointerSectionType.FIELD),
        PointerSection("2", PointerSectionType.SEQUENCE),
        PointerSection("documentProduced", PointerSectionType.FIELD),
        PointerSection("1", PointerSectionType.SEQUENCE),
        PointerSection("documentStatus", PointerSectionType.FIELD)
      )
      val pointer = Pointer(sections)
      val url = "/customs-declare-exports/declaration/items/ITEM_ID/add-document"
      val expectedItemId = "itemId2"
      val declaration =
        aDeclaration(withItems(anItem(withSequenceId(1), withItemId("itemId1")), anItem(withSequenceId(2), withItemId(expectedItemId))))

      val expectedUrl = s"/customs-declare-exports/declaration/items/$expectedItemId/add-document"

      RejectionReason.url(url, declaration, Some(pointer)) mustBe expectedUrl
    }

    "return default url for item" when {

      "declaration doesn't have that item" in {

        val sections = Seq(
          PointerSection("declaration", PointerSectionType.FIELD),
          PointerSection("items", PointerSectionType.FIELD),
          PointerSection("1", PointerSectionType.SEQUENCE),
          PointerSection("documentProduced", PointerSectionType.FIELD),
          PointerSection("1", PointerSectionType.SEQUENCE),
          PointerSection("documentStatus", PointerSectionType.FIELD)
        )
        val pointer = Pointer(sections)
        val url = "/customs-declare-exports/declaration/items/ITEM_ID/add-document"
        val declaration = aDeclaration()

        val expectedUrl = s"/customs-declare-exports/declaration/export-items"

        RejectionReason.url(url, declaration, Some(pointer)) mustBe expectedUrl
      }

      "pointer is missing" in {

        val url = "/customs-declare-exports/declaration/containers/ITEM_ID/seals"
        val declaration =
          aDeclaration(withItems(anItem(withSequenceId(1), withItemId("itemId1")), anItem(withSequenceId(2), withItemId("itemId2"))))

        val expectedUrl = "/customs-declare-exports/declaration/export-items"

        RejectionReason.url(url, declaration, None) mustBe expectedUrl
      }
    }

    "correctly build url for container" in {

      val sections = Seq(
        PointerSection("declaration", PointerSectionType.FIELD),
        PointerSection("containers", PointerSectionType.FIELD),
        PointerSection("2", PointerSectionType.SEQUENCE),
        PointerSection("seals", PointerSectionType.FIELD),
        PointerSection("1", PointerSectionType.SEQUENCE),
        PointerSection("id", PointerSectionType.FIELD)
      )
      val pointer = Pointer(sections)
      val url = "/customs-declare-exports/declaration/containers/CONTAINER_ID/seals"
      val expectedContainerId = "containerId2"
      val declaration = aDeclaration(withContainerData(Seq(Container("containerId1", Seq.empty), Container(expectedContainerId, Seq(Seal("1234"))))))

      val expectedUrl = s"/customs-declare-exports/declaration/containers/$expectedContainerId/seals"

      RejectionReason.url(url, declaration, Some(pointer)) mustBe expectedUrl
    }

    "return default url for container" when {

      "declaration doesn't have that container" in {

        val sections = Seq(
          PointerSection("declaration", PointerSectionType.FIELD),
          PointerSection("items", PointerSectionType.FIELD),
          PointerSection("1", PointerSectionType.SEQUENCE),
          PointerSection("documentProduced", PointerSectionType.FIELD),
          PointerSection("1", PointerSectionType.SEQUENCE),
          PointerSection("documentStatus", PointerSectionType.FIELD)
        )
        val pointer = Pointer(sections)
        val url = "/customs-declare-exports/declaration/containers/CONTAINER_ID/seals"
        val declaration = aDeclaration()

        val expectedUrl = s"/customs-declare-exports/declaration/containers"

        RejectionReason.url(url, declaration, Some(pointer)) mustBe expectedUrl
      }

      "pointer is missing" in {

        val url = "/customs-declare-exports/declaration/containers/CONTAINER_ID/seals"
        val declaration = aDeclaration(withContainerData(Seq(Container("containerId1", Seq.empty), Container("containerId2", Seq(Seal("1234"))))))

        val expectedUrl = "/customs-declare-exports/declaration/containers"

        RejectionReason.url(url, declaration, None) mustBe expectedUrl
      }
    }

    "correctly build url page which is not related with items or containers" in {

      val pointer = Pointer(Seq.empty)
      val url = "/customs-declare-exports/declaration/consignmentReferences"
      val declaration = aDeclaration()

      RejectionReason.url(url, declaration, Some(pointer)) mustBe url
    }
  }
}
