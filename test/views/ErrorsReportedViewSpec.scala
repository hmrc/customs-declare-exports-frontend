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

import base.Injector
import controllers.routes.{DeclarationDetailsController, SubmissionsController}
import connectors.CodeListConnector
import models.declaration.errors.ErrorInstance
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import services.cache.ExportsTestHelper
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.errors_reported

class ErrorsReportedViewSpec extends UnitViewSpec with ExportsTestHelper with Injector with Stubs {

  private val page = instanceOf[errors_reported]
  private val codeListConnector = mock[CodeListConnector]

  private val declaration = aDeclaration(withConsignmentReferences("DUCR", "lrn"))

  private def view(
    reasons: Seq[ErrorInstance] = Seq.empty,
    maybeDeclarationId: Option[String] = None,
    testMessages: Messages = messages,
    maybeSubmissionId: Option[String] = None
  ): Document =
    page(maybeSubmissionId, declaration, MRN.value, maybeDeclarationId, reasons)(request, testMessages, codeListConnector)

  val submissionId = "submissionId"
  val defaultRejectionCode = "CDS10001"

  val defaultView: Document = view()
  val amendmentView: Document = view(maybeDeclarationId = Some("declarationId"), maybeSubmissionId = Some(submissionId))

  "Errors Reported page" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("rejected.notification.mrn.missing")
      messages must haveTranslationFor("rejected.notification.title")
      messages must haveTranslationFor("rejected.amendment.title")
      messages must haveTranslationFor("rejected.notification.table.title")
      messages must haveTranslationFor("rejected.notification.warning")
      messages must haveTranslationFor("rejected.amendment.warning")
      messages must haveTranslationFor("rejected.notification.description.heading")
      messages must haveTranslationFor("rejected.notification.check.answers.paragraph")
      messages must haveTranslationFor("rejected.amendment.check.answers.paragraph")
      messages must haveTranslationFor("rejected.notification.check.answers.button")

      messages must haveTranslationFor("rejected.notification.guidance.section.1.header")
      messages must haveTranslationFor("rejected.notification.guidance.section.1.paragraph.1")

      messages must haveTranslationFor("rejected.notification.guidance.section.2.header")
      messages must haveTranslationFor("rejected.notification.guidance.section.2.paragraph.1")
      messages must haveTranslationFor("rejected.notification.guidance.section.2.paragraph.1.link")
      messages must haveTranslationFor("rejected.amendment.guidance.section.2.paragraph.1")

      messages must haveTranslationFor("rejected.notification.guidance.section.3.header")
      messages must haveTranslationFor("rejected.notification.guidance.section.3.paragraph.1")
      messages must haveTranslationFor("rejected.notification.guidance.section.3.paragraph.2")
    }

    "have correct title" in {
      defaultView.getElementById("title").text mustBe messages("rejected.notification.title")
      amendmentView.getElementById("title").text mustBe messages("rejected.amendment.title")
    }

    "have correct warning" in {
      defaultView.getElementsByClass("govuk-warning-text").first() must containText(messages("rejected.notification.warning"))
      amendmentView.getElementsByClass("govuk-warning-text").first() must containText(messages("rejected.amendment.warning"))
    }

    "have correct section header" in {
      defaultView.getElementById("section-header").text mustBe messages("mrn.heading", MRN.value)
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
      headings.get(0).text() mustBe messages("rejected.notification.guidance.section.1.header")
      headings.get(1).text() mustBe messages("rejected.notification.guidance.section.2.header")
      headings.get(2).text() mustBe messages("rejected.notification.guidance.section.3.header")
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

      val email = messages("rejected.notification.guidance.section.3.paragraph.2.email")
      body.get(4).text() mustBe messages("rejected.notification.guidance.section.3.paragraph.2", email)
      val emailElement = body.get(4).getElementsByClass("govuk-link").get(0)
      emailElement.getElementsByAttributeValue("href", s"mailto:$email")

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
  }
}
