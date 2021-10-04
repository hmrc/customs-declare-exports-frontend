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

package services.model

import base.{Injector, JourneyTypeTestRunner, UnitWithMocksSpec}
import controllers.declaration.routes
import forms.common.Eori
import models.DeclarationType.{OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.declaration.notifications.{Notification, NotificationError}
import models.declaration.submissions.SubmissionStatus
import models.{DeclarationType, Pointer, PointerSection, PointerSectionType}
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito.given
import play.api.i18n.Messages
import services.cache.ExportsTestData

import java.time.{ZoneOffset, ZonedDateTime}

class RejectionReasonSpec extends UnitWithMocksSpec with ExportsTestData with JourneyTypeTestRunner with Injector {

  private val messages = mock[Messages]

  private val itemsListUrl = routes.ItemsSummaryController.displayItemsSummaryPage().url
  private val declarantDetailsUrl = routes.DeclarantDetailsController.displayPage().url
  private val personPresentingGoodsUrl = routes.PersonPresentingGoodsDetailsController.displayPage().url

  "RejectionReason" when {
    "fromNotification called" should {
      "handle an empty list of notifications" in {
        RejectionReason.fromNotifications(Seq.empty)(messages) mustBe Seq.empty
      }

      "handle notifications that are not rejections" in {
        val acceptedNotification =
          Notification("convId", "mrn", ZonedDateTime.now(ZoneOffset.UTC), SubmissionStatus.ACCEPTED, Seq.empty)

        RejectionReason.fromNotifications(Seq(acceptedNotification))(messages) mustBe Seq.empty
      }

      "handle a rejection notification" when {
        "no errors are present" in {
          val notification =
            Notification("actionId", "mrn", ZonedDateTime.now(ZoneOffset.UTC), SubmissionStatus.REJECTED, Seq.empty)

          RejectionReason.fromNotifications(Seq(notification))(messages) mustBe Seq.empty
        }

        "pointer is known" in {
          given(messages.isDefinedAt("field.x.$.z")).willReturn(true)
          val error = NotificationError("CDS12016", Some(Pointer("x.#0.z")), None)
          val notification =
            Notification("actionId", "mrn", ZonedDateTime.now(ZoneOffset.UTC), SubmissionStatus.REJECTED, Seq(error))

          RejectionReason.fromNotifications(Seq(notification))(messages) mustBe Seq(RejectionReason("CDS12016", Some(Pointer("x.#0.z")), None))
        }

        "pointer is unknown" in {
          given(messages.isDefinedAt(anyString())).willReturn(false)
          val error = NotificationError("CDS12016", Some(Pointer("x.#0.z")), None)
          val notification =
            Notification("actionId", "mrn", ZonedDateTime.now(ZoneOffset.UTC), SubmissionStatus.REJECTED, Seq(error))

          RejectionReason.fromNotifications(Seq(notification))(messages) mustBe Seq(RejectionReason("CDS12016", None, None))
        }

        "pointer is empty" in {
          val error = NotificationError("CDS12016", None, None)
          val notification =
            Notification("actionId", "mrn", ZonedDateTime.now(ZoneOffset.UTC), SubmissionStatus.REJECTED, Seq(error))

          RejectionReason.fromNotifications(Seq(notification))(messages) mustBe Seq(RejectionReason("CDS12016", None, None))
        }
      }
    }

    "specialiseUrl called" should {
      val pointer = Pointer(
        Seq(
          PointerSection("declaration", PointerSectionType.FIELD),
          PointerSection("items", PointerSectionType.FIELD),
          PointerSection("1", PointerSectionType.SEQUENCE),
          PointerSection("additionalDocument", PointerSectionType.FIELD),
          PointerSection("1", PointerSectionType.SEQUENCE),
          PointerSection("documentStatus", PointerSectionType.FIELD)
        )
      )

      val itemTemplateUrl = "/customs-declare-exports/declaration/items/[ITEM_ID]/add-document"
      val partiesTemplateurl = "/customs-declare-exports/declaration/[URL_PATH]"

      "return the url unmodified" when {
        "url does not contain any placeholders" in {
          val url = "/customs-declare-exports/declaration/items/no-place-holder/add-document"

          RejectionReason.specialiseUrl(url, aDeclaration(), Some(pointer)) mustBe url
        }
      }

      "return default url for items" when {
        "declaration doesn't have that item" in {
          RejectionReason.specialiseUrl(itemTemplateUrl, aDeclaration(), Some(pointer)) mustBe itemsListUrl
        }

        "pointer is missing" in {
          RejectionReason.specialiseUrl(itemTemplateUrl, aDeclaration(), None) mustBe itemsListUrl
        }

        "pointer has no sequences" in {
          val pointer = Pointer(Seq.empty)

          RejectionReason.specialiseUrl(itemTemplateUrl, aDeclaration(), Some(pointer)) mustBe itemsListUrl
        }
      }

      "correctly build url for item" in {
        val sections = Seq(
          PointerSection("declaration", PointerSectionType.FIELD),
          PointerSection("items", PointerSectionType.FIELD),
          PointerSection("2", PointerSectionType.SEQUENCE),
          PointerSection("additionalDocument", PointerSectionType.FIELD),
          PointerSection("1", PointerSectionType.SEQUENCE),
          PointerSection("documentStatus", PointerSectionType.FIELD)
        )
        val pointer = Pointer(sections)
        val expectedItemId = "itemId2"
        val declaration =
          aDeclaration(withItems(anItem(withSequenceId(1), withItemId("itemId1")), anItem(withSequenceId(2), withItemId(expectedItemId))))

        val expectedUrl = s"/customs-declare-exports/declaration/items/$expectedItemId/add-document"

        RejectionReason.specialiseUrl(itemTemplateUrl, declaration, Some(pointer)) mustBe expectedUrl
      }

      val sections = Seq(
        PointerSection("declaration", PointerSectionType.FIELD),
        PointerSection("declarantDetails", PointerSectionType.FIELD),
        PointerSection("details", PointerSectionType.FIELD),
        PointerSection("eori", PointerSectionType.FIELD)
      )

      onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
        "called" must {
          s"correctly supply the 'declarantDetailsUrl' url for parties" in {
            val pointer = Pointer(sections)
            val declaration = aDeclaration(withType(request.declarationType))

            RejectionReason.specialiseUrl(partiesTemplateurl, declaration, Some(pointer)) mustBe declarantDetailsUrl
          }
        }
      }

      "on Clearance journey" when {
        "declaration is CLEARANCE and isEXS and some PersonPresentingGoodsDetails are present" must {
          "correctly supply the 'personPresentingGoodsUrl' url for parties when " in {
            val pointer = Pointer(sections)
            val declaration =
              aDeclaration(withType(DeclarationType.CLEARANCE), withIsExs(), withPersonPresentingGoodsDetails(Some(Eori("GB201920200000"))))

            RejectionReason.specialiseUrl(partiesTemplateurl, declaration, Some(pointer)) mustBe personPresentingGoodsUrl
          }
        }
      }
    }
  }
}
