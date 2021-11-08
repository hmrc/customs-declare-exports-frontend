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

package views

import base.Injector
import controllers.declaration.{routes => decRoutes}
import controllers.routes
import models.Pointer
import models.declaration.notifications.NotificationError
import models.Mode.Normal
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.rejected_notification_errors

class RejectedNotificationErrorsViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = instanceOf[rejected_notification_errors]
  private val declaration = aDeclaration(withConsignmentReferences("DUCR", "lrn"))

  private def view(
    reasons: Seq[NotificationError] = Seq.empty,
    maybeMrn: Option[String] = Some(MRN.value),
    testMessages: Messages = messages
  ): Document =
    page(declaration, maybeMrn, reasons)(request, testMessages)

  val defaultView: Document = view()
  val defaultRejectionCode = "CDS10001"

  "Rejected notification errors page" should {
    "have proper messages for labels" in {
      messages must haveTranslationFor("rejected.notification.mrn")
      messages must haveTranslationFor("rejected.notification.mrn.missing")
      messages must haveTranslationFor("rejected.notification.title")
      messages must haveTranslationFor("rejected.notification.table.title")
      messages must haveTranslationFor("rejected.notification.warning")
      messages must haveTranslationFor("rejected.notification.description.format")

      messages must haveTranslationFor("rejected.notification.guidance.section.1.header")
      messages must haveTranslationFor("rejected.notification.guidance.section.1.paragraph.1")
      messages must haveTranslationFor("rejected.notification.guidance.section.1.paragraph.2")
      messages must haveTranslationFor("rejected.notification.guidance.section.1.paragraph.2.link.1")
      messages must haveTranslationFor("rejected.notification.guidance.section.1.paragraph.2.link.2")

      messages must haveTranslationFor("rejected.notification.guidance.section.2.header")
      messages must haveTranslationFor("rejected.notification.guidance.section.2.paragraph.1")
      messages must haveTranslationFor("rejected.notification.guidance.section.2.paragraph.2")
      messages must haveTranslationFor("rejected.notification.guidance.section.2.paragraph.3")
    }

    "have correct title" in {
      defaultView.getElementById("title").text() mustBe messages("rejected.notification.title")
    }

    "have correct section header" in {
      defaultView.getElementById("section-header").text() mustBe messages("rejected.notification.mrn", MRN.value)
    }

    "have correct back link" in {
      val backLink = defaultView.getElementById("back-link")

      backLink.text() mustBe messages("site.back")
      backLink.attr("href") mustBe routes.DeclarationDetailsController.displayPage(declaration.id).url
    }

    "must contain notifications" when {
      val reason =
        NotificationError(defaultRejectionCode, Some(Pointer("declaration.consignmentReferences.lrn")))

      val testMessages = stubMessages()

      "fully populated and we are using the exports error descriptions" in {
        val doc: Document = view(Seq(reason), Some(MRN.value), testMessages)

        doc.getElementsByClass("rejected_notifications-row-0-name").text() mustBe testMessages("field.declaration.consignmentReferences.lrn")
        doc.getElementsByClass("rejected_notifications-row-0-description").isEmpty mustBe false
      }

      "pointer " in {
        val reason =
          NotificationError(defaultRejectionCode, Some(Pointer("declaration.goodsShipment.governmentAgencyGoodsItem.#0.additionalDocument.#1.id")))

        val doc: Document = view(Seq(reason), Some(MRN.value), testMessages)

        doc.getElementsByClass("rejected_notifications-row-0-name").text() mustBe testMessages(
          "field.declaration.goodsShipment.governmentAgencyGoodsItem.$.additionalDocument.$.id",
          "0",
          "1"
        )
      }
    }

    "must contain continue link" in {
      val continueLink = defaultView
        .getElementById("continue-checking-answers")
        .getElementsByAttributeValue("href", routes.SubmissionsController.amend(declaration.id).url)

      continueLink.text() mustBe messages("rejected.notification.guidance.section.1.paragraph.2.link.1")
    }

    "display all other expected content links" in {
      val reason =
        NotificationError(defaultRejectionCode, Some(Pointer("declaration.consignmentReferences.lrn")))

      val doc: Document = view(Seq(reason), Some(MRN.value))

      val links = doc.getElementsByClass("govuk-link--no-visited-state")
      links.size mustBe 3

      links.get(1) must haveHref(controllers.routes.SubmissionsController.amend(declaration.id).url)
      links.get(2) must haveHref(routes.SavedDeclarationsController.displayDeclarations())
    }

    "contain change error link" when {
      "link for the error exists" in {
        val itemId = "12sd31"
        val declaration = aDeclaration(withConsignmentReferences("DUCR", "lrn"), withItem(anItem(withSequenceId(1), withItemId(itemId))))

        val expectedUrl = decRoutes.AdditionalDocumentsController.displayPage(Normal, itemId)

        val pointerPattern = "declaration.items.#1.additionalDocument.#1.documentStatus"
        val urlPattern = "declaration.items.$.additionalDocument.$.documentStatus"

        val noteError = NotificationError("CDS12062", Some(Pointer(pointerPattern)))

        val view: Document = page(declaration, Some(MRN.value), Seq(noteError))(request, messages)

        val changeLink = view.getElementsByClass("govuk-link").get(1)

        changeLink must haveHref(
          controllers.routes.SubmissionsController.amendErrors(declaration.id, expectedUrl.url, urlPattern, messages("dmsError.CDS12062.title")).url
        )
      }
    }
  }
}
