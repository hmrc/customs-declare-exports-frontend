/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.declaration.routes.AdditionalDocumentsController
import controllers.routes.{DeclarationDetailsController, SavedDeclarationsController, SubmissionsController}
import models.Pointer
import models.declaration.notifications.NotificationError
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestHelper
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.rejected_notification_errors

class RejectedNotificationErrorsViewSpec extends UnitViewSpec with ExportsTestHelper with Injector with Stubs {

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
      messages must haveTranslationFor("rejected.notification.check.answers.paragraph")
      messages must haveTranslationFor("rejected.notification.check.answers.button")

      messages must haveTranslationFor("rejected.notification.guidance.section.1.header")
      messages must haveTranslationFor("rejected.notification.guidance.section.1.paragraph.1")
      messages must haveTranslationFor("rejected.notification.guidance.section.1.paragraph.2")
      messages must haveTranslationFor("rejected.notification.guidance.section.1.paragraph.2.link")

      messages must haveTranslationFor("rejected.notification.guidance.section.2.header")
      messages must haveTranslationFor("rejected.notification.guidance.section.2.paragraph.1")
      messages must haveTranslationFor("rejected.notification.guidance.section.2.paragraph.2")
      messages must haveTranslationFor("rejected.notification.guidance.section.2.paragraph.3")
    }

    "have correct title" in {
      defaultView.getElementById("title").text mustBe messages("rejected.notification.title")
    }

    "have correct section header" in {
      defaultView.getElementById("section-header").text mustBe messages("rejected.notification.mrn", MRN.value)
    }

    "have correct back link" in {
      val backLink = defaultView.getElementById("back-link")

      backLink.text mustBe messages("site.back")
      backLink.attr("href") mustBe DeclarationDetailsController.displayPage(declaration.id).url
    }

    "contain notifications" when {
      val reason = NotificationError(defaultRejectionCode, Some(Pointer("declaration.consignmentReferences.lrn")))

      val testMessages = stubMessages()

      "fully populated and we are using the exports error descriptions" in {
        val doc: Document = view(Seq(reason), Some(MRN.value), testMessages)

        val text = doc.getElementsByClass("rejected_notifications-row-0-name").text
        text mustBe testMessages("field.declaration.consignmentReferences.lrn")
        doc.getElementsByClass("rejected_notifications-row-0-description").isEmpty mustBe false
      }

      "pointer " in {
        val pointer = Pointer("declaration.goodsShipment.governmentAgencyGoodsItem.#0.additionalDocument.#1.id")
        val reason = NotificationError(defaultRejectionCode, Some(pointer))

        val doc: Document = view(Seq(reason), Some(MRN.value), testMessages)

        doc.getElementsByClass("rejected_notifications-row-0-name").text mustBe testMessages(
          "field.declaration.goodsShipment.governmentAgencyGoodsItem.$.additionalDocument.$.id",
          "0",
          "1"
        )
      }
    }

    "contain the 'check-your-answers' paragraph" in {
      val checkYourAnswers = defaultView.getElementsByClass("govuk-body").get(0)
      checkYourAnswers.text mustBe messages("rejected.notification.check.answers.paragraph")
    }

    "contain the 'check-your-answers' button" in {
      val checkYourAnswers = defaultView.getElementById("check-your-answers")
      checkYourAnswers.className mustBe "govuk-button"

      val href = checkYourAnswers.getElementsByAttributeValue("href", SubmissionsController.amend(declaration.id).url)
      href.text mustBe messages("rejected.notification.check.answers.button")
    }

    "display all other expected content links" in {
      val reason =
        NotificationError(defaultRejectionCode, Some(Pointer("declaration.consignmentReferences.lrn")))

      val doc: Document = view(Seq(reason), Some(MRN.value))

      val links = doc.getElementsByClass("govuk-link--no-visited-state")
      links.size mustBe 2

      links.get(1) must haveHref(SavedDeclarationsController.displayDeclarations())
    }

    "contain change error link" when {
      "link for the error exists" in {
        val itemId = "12sd31"
        val item = withItem(anItem(withSequenceId(1), withItemId(itemId)))
        val declaration = aDeclaration(withConsignmentReferences("DUCR", "lrn"), item)

        val `expectedUrl` = AdditionalDocumentsController.displayPage(itemId)

        val pointerPattern = "declaration.items.#1.additionalDocument.#1.documentStatus"
        val urlPattern = "declaration.items.$.additionalDocument.$.documentStatus"

        val noteError = NotificationError("CDS12062", Some(Pointer(pointerPattern)))

        val view: Document = page(declaration, Some(MRN.value), Seq(noteError))(request, messages)

        val changeLink = view.getElementsByClass("govuk-link").get(2)

        changeLink must haveHref(
          SubmissionsController.amendErrors(declaration.id, expectedUrl.url, urlPattern, messages("dmsError.CDS12062.title")).url
        )
      }
    }
  }
}
