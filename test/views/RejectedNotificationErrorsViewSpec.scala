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

package views

import base.{Injector, OverridableInjector}
import config.featureFlags.TdrFeatureFlags
import controllers.declaration.routes.AdditionalDocumentsController
import controllers.routes.{DeclarationDetailsController, DraftDeclarationController, SubmissionsController}
import models.Pointer
import models.declaration.notifications.NotificationError
import org.jsoup.nodes.Document
import org.mockito.Mockito.when
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestHelper
import tools.Stubs
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import views.declaration.spec.UnitViewSpec
import views.html.rejected_notification_errors
import play.api.inject.bind

class RejectedNotificationErrorsViewSpec extends UnitViewSpec with ExportsTestHelper with Injector with Stubs {

  private val page = instanceOf[rejected_notification_errors]

  private val declaration = aDeclaration(withConsignmentReferences("DUCR", "lrn"))

  implicit val injector: OverridableInjector = new OverridableInjector(bind[TdrFeatureFlags].toInstance(mockTdrFeatureFlags))

  private def view(
    reasons: Seq[NotificationError] = Seq.empty,
    maybeDeclarationId: Option[String] = None,
    testMessages: Messages = messages,
    maybeSubmissionId: Option[String] = None
  )(implicit injector: OverridableInjector): Document = {
    val page = injector.instanceOf[rejected_notification_errors]
    page(maybeSubmissionId, declaration, MRN.value, maybeDeclarationId, reasons)(request, testMessages)
  }

  val submissionId = "submissionId"
  val defaultRejectionCode = "CDS10001"

  val defaultView: Document = view()
  val amendmentView: Document = view(maybeDeclarationId = Some("declarationId"), maybeSubmissionId = Some(submissionId))

  "Rejected notification errors page" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("rejected.notification.mrn.missing")
      messages must haveTranslationFor("rejected.notification.description.format")

      messages must haveTranslationFor("rejected.amendment.guidance.section.2.paragraph.1")
    }

    "have correct title" in {
      defaultView.getElementById("title").text mustBe messages("rejected.notification.v1.title")
      amendmentView.getElementById("title").text mustBe messages("rejected.amendment.title")
    }

    "have correct warning" in {
      defaultView.getElementsByClass("govuk-warning-text").first must containText(messages("rejected.notification.warning"))
      amendmentView.getElementsByClass("govuk-warning-text").first must containText(messages("rejected.amendment.warning"))
    }

    "have the expected MRN hint" in {
      defaultView.getElementsByClass("submission-mrn").text mustBe messages("mrn.heading", MRN.value)
    }

    "have correct back link" in {
      val backLink = defaultView.getElementById("back-link")

      backLink.text mustBe messages("site.back")
      backLink.attr("href") mustBe DeclarationDetailsController.displayPage(declaration.id).url
    }

    "have correct back link for amended declarations which has been rejected" in {
      val backLink = amendmentView.getElementById("back-link")

      backLink.text mustBe messages("site.back")
      backLink.attr("href") mustBe DeclarationDetailsController.displayPage(submissionId).url
    }

    "have the expected headings" in {
      val headingm = defaultView.getElementsByClass("govuk-heading-m").get(0).text()
      headingm mustBe messages("rejected.notification.table.title")

      val headings = defaultView.getElementsByClass("govuk-heading-s")
      headings.get(0).text() mustBe messages("rejected.notification.banner.title")
      headings.get(1).text() mustBe messages("rejected.notification.guidance.section.1.header")
    }

    "have the expected body content" in {
      val body = defaultView.getElementsByClass("govuk-body")
      body.get(0).text() mustBe messages("rejected.notification.check.answers.paragraph")

      body.get(1).text() mustBe messages("rejected.notification.guidance.section.1.paragraph.1")
      body.get(2).text() mustBe messages(
        "rejected.notification.guidance.section.2.paragraph.1",
        messages("rejected.notification.guidance.section.2.paragraph.1.link")
      )

      body.get(3).text() mustBe messages("rejected.notification.guidance.section.3.paragraph.1")
      body.get(4).text() mustBe messages("rejected.notification.guidance.section.3.paragraph.2")
      body.get(5).text() mustBe messages("rejected.notification.guidance.section.3.paragraph.3")
    }

    "have the expected body content in TDR" in {
      when(mockTdrFeatureFlags.showErrorPageVersionForTdr).thenReturn(true)
      val tdrPage = view()
      val body = tdrPage.getElementsByClass("govuk-body")

      val email = messages("rejected.notification.tdr.guidance.section.3.paragraph.2.email")
      body.get(4).text() mustBe messages("rejected.notification.tdr.guidance.section.3.paragraph.2", email)
      val emailElement = body.get(4).getElementsByClass("govuk-link").get(0)
      emailElement.getElementsByAttributeValue("href", s"mailto:$email")
    }

    "contain notifications" when {
      val reason = NotificationError(defaultRejectionCode, Some(Pointer("declaration.consignmentReferences.lrn")))

      val testMessages = stubMessages()

      "fully populated and we are using the exports error descriptions" in {
        val doc: Document = view(Seq(reason), None, testMessages)

        val text = doc.getElementsByClass("rejected_notifications-row-0-name").text
        text mustBe testMessages("field.declaration.consignmentReferences.lrn")
        doc.getElementsByClass("rejected_notifications-row-0-description").isEmpty mustBe false
      }

      "pointer " in {
        val pointer = Pointer("declaration.goodsShipment.governmentAgencyGoodsItem.#0.additionalDocument.#1.id")
        val reason = NotificationError(defaultRejectionCode, Some(pointer))

        val doc: Document = view(Seq(reason), None, testMessages)

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

      val checkYourAmendment = amendmentView.getElementsByClass("govuk-body").get(0)
      checkYourAmendment.text mustBe messages("rejected.amendment.check.answers.paragraph")
    }

    "contain the 'check-your-answers' button" in {
      val checkYourAnswers = defaultView.getElementById("check-your-answers")
      checkYourAnswers.className mustBe "govuk-button"

      val href = checkYourAnswers.getElementsByAttributeValue("href", SubmissionsController.amend(declaration.id, false).url)
      href.text mustBe messages("rejected.notification.check.answers.button")
    }

    "contain the 'check-your-answers' button for amended declarations which has been rejected" in {
      val checkYourAnswers = amendmentView.getElementById("check-your-answers")
      checkYourAnswers.className mustBe "govuk-button"

      val href = checkYourAnswers.getElementsByAttributeValue("href", SubmissionsController.amend(declaration.id, true).url)
      href.text mustBe messages("rejected.notification.check.answers.button")
    }

    "display all other expected content links" in {
      when(mockTdrFeatureFlags.showErrorPageVersionForTdr).thenReturn(false)
      val reason = NotificationError(defaultRejectionCode, Some(Pointer("declaration.consignmentReferences.lrn")))

      val document = view(Seq(reason))

      val links = document.getElementsByClass("govuk-link--no-visited-state")
      links.size mustBe 3
      links.get(2) must haveHref(DraftDeclarationController.displayDeclarations())
    }

    "contain change error link" when {
      val itemId = "12sd31"
      val item = withItem(anItem(withSequenceId(1), withItemId(itemId)))
      val declaration = aDeclaration(withConsignmentReferences("DUCR", "lrn"), item)

      val `expectedUrl` = AdditionalDocumentsController.displayPage(itemId)

      val pointerPattern = "declaration.items.#1.additionalDocument.#1.documentStatus"
      val urlPattern = "declaration.items.$.additionalDocument.$.documentStatus"

      val noteError = NotificationError("CDS12062", Some(Pointer(pointerPattern)))

      "link for the error exists" in {
        val view = page(None, declaration, MRN.value, None, Seq(noteError))(request, messages)

        val changeLink = view.getElementsByClass("govuk-link").get(4)
        changeLink must haveHref(
          SubmissionsController.amendErrors(declaration.id, urlPattern, messages("dmsError.CDS12062.title"), false, RedirectUrl(expectedUrl.url)).url
        )
      }

      "link for the error exists for amended declarations which has been rejected" in {
        val view = page(Some(submissionId), declaration, MRN.value, Some("declarationId"), Seq(noteError))(request, messages)

        val changeLink = view.getElementsByClass("govuk-link").get(4)
        changeLink must haveHref(
          SubmissionsController.amendErrors(declaration.id, urlPattern, messages("dmsError.CDS12062.title"), true, RedirectUrl(expectedUrl.url)).url
        )
      }
    }
  }
}
