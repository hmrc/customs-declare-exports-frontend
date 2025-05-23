/*
 * Copyright 2024 HM Revenue & Customs
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

package views.timeline

import base.Injector
import connectors.CodeListConnector
import controllers.routes.SavedDeclarationsController
import controllers.timeline.routes.{DeclarationDetailsController, SubmissionsController}
import models.Pointer
import models.codes.DmsErrorCode
import models.declaration.errors.{ErrorInstance, FieldInvolved}
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.i18n.Messages
import services.cache.ExportsTestHelper
import views.common.UnitViewSpec
import views.html.timeline.errors_reported

import scala.collection.immutable.ListMap

class ErrorsReportedViewSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  private val codeListConnector = mock[CodeListConnector]
  private val declaration = aDeclaration(withConsignmentReferences("DUCR", "lrn"), withItems(2))

  private val errorCode = "CDS12056"
  private val errorMap = {
    val dmsErrorCode = DmsErrorCode(errorCode, "Relation Error: The combination of elements is not allowed")
    ListMap(errorCode -> dmsErrorCode)
  }

  when(codeListConnector.getDmsErrorCodesMap(any())).thenReturn(errorMap)

  private val errorInstances = {
    val fieldPointer = "declaration.items.#1.additionalDocument.#2.documentStatus"
    val field = FieldInvolved(Pointer(fieldPointer), Some("ABC"), Some("CBA"), None, None)
    List(ErrorInstance(declaration, 1, errorCode, List(field)))
  }

  private val page = instanceOf[errors_reported]

  private def view(
    errorInstances: Seq[ErrorInstance] = errorInstances,
    maybeDeclarationId: Option[String] = None,
    testMessages: Messages = messages,
    maybeSubmissionId: Option[String] = None
  ): Document =
    page(maybeSubmissionId, declaration, MRN.value, maybeDeclarationId, errorInstances)(request, testMessages, codeListConnector)

  private val submissionId = "submissionId"

  private val defaultView: Document = view()

  private val amendmentView: Document = view(maybeDeclarationId = Some("declarationId"), maybeSubmissionId = Some(submissionId))

  "Errors Reported page" when {
    "have proper messages for labels" in {
      messages must haveTranslationFor("rejected.notification.mrn.missing")
      messages must haveTranslationFor("rejected.notification.title")
      messages must haveTranslationFor("rejected.amendment.title")
      messages must haveTranslationFor("rejected.notification.table.title")
      messages must haveTranslationFor("rejected.notification.warning")
      messages must haveTranslationFor("rejected.amendment.warning")
      messages must haveTranslationFor("rejected.notification.description.heading")
      messages must haveTranslationFor("rejected.notification.check.answers.button")

      messages must haveTranslationFor("rejected.notification.guidance.section.1.header")
      messages must haveTranslationFor("rejected.notification.guidance.section.1.paragraph.1")
      messages must haveTranslationFor("rejected.notification.guidance.section.1.paragraph.1.link")

      messages must haveTranslationFor("rejected.notification.guidance.section.2.header")
      messages must haveTranslationFor("rejected.notification.guidance.section.2.paragraph.1")
      messages must haveTranslationFor("rejected.notification.guidance.section.2.paragraph.2")
      messages must haveTranslationFor("rejected.notification.guidance.section.2.link.1")

      messages must haveTranslationFor("rejected.amendment.guidance.section.2.paragraph.1")

      messages must haveTranslationFor("rejected.notification.guidance.section.3.header")
      messages must haveTranslationFor("rejected.notification.guidance.section.3.link.1")
      messages must haveTranslationFor("rejected.notification.guidance.section.3.link.2")
      messages must haveTranslationFor("rejected.notification.guidance.section.3.paragraph.1")
      messages must haveTranslationFor("rejected.notification.guidance.section.3.paragraph.2")
      messages must haveTranslationFor("rejected.notification.guidance.section.3.paragraph.3")
    }

    "have correct title" in {
      defaultView.getElementById("title").text mustBe messages("rejected.notification.title")
      amendmentView.getElementById("title").text mustBe messages("rejected.amendment.title")
    }

    "have correct warning" in {
      defaultView.getElementsByClass("govuk-warning-text").first() must containText(messages("rejected.notification.warning"))
      amendmentView.getElementsByClass("govuk-warning-text").first() must containText(messages("rejected.amendment.warning"))
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
      defaultView.getElementsByClass("govuk-heading-m").get(0).text() mustBe messages("rejected.notification.table.title")

      val headings = defaultView.getElementsByClass("govuk-heading-s")
      headings.size mustBe 3
      headings.get(0).text mustBe messages("rejected.notification.guidance.section.1.header")
      headings.get(1).text mustBe messages("rejected.notification.guidance.section.2.header")
      headings.get(2).text mustBe messages("rejected.notification.guidance.section.3.header")
    }

    "have the expected body content" in {
      val body = defaultView.getElementsByClass("govuk-body")
      body.size mustBe 9

      // Section 1
      body.get(0).text mustBe messages(
        "rejected.notification.guidance.section.1.paragraph.1",
        messages("rejected.notification.guidance.section.1.paragraph.1.link")
      )
      val draftDeclarationLink = body.get(0).getElementsByClass("govuk-link").get(0)
      draftDeclarationLink.getElementsByAttributeValue("href", SavedDeclarationsController.displayDeclarations().url)

      // Section 2
      body.get(1).text mustBe messages("rejected.notification.guidance.section.2.paragraph.1")
      body.get(2).text mustBe messages("rejected.notification.guidance.section.2.paragraph.2")

      val reportProblemsByUsingCDS = body.get(3).getElementsByClass("govuk-link").get(0)
      reportProblemsByUsingCDS.text mustBe messages("rejected.notification.guidance.section.2.link.1")
      reportProblemsByUsingCDS.getElementsByAttributeValue("href", minimalAppConfig.reportProblemsByUsingCDS)

      // Section 3
      val errorWorkaroundsForCDS = body.get(4).getElementsByClass("govuk-link").get(0)
      errorWorkaroundsForCDS.text mustBe messages("rejected.notification.guidance.section.3.link.1")
      errorWorkaroundsForCDS.getElementsByAttributeValue("href", minimalAppConfig.errorWorkaroundsForCDS)

      val errorCodesForCDS = body.get(5).getElementsByClass("govuk-link").get(0)
      errorCodesForCDS.text mustBe messages("rejected.notification.guidance.section.3.link.2")
      errorCodesForCDS.getElementsByAttributeValue("href", minimalAppConfig.errorCodesForCDS)

      body.get(6).text mustBe messages("rejected.notification.guidance.section.3.paragraph.1")
      body.get(7).text mustBe messages("rejected.notification.guidance.section.3.paragraph.2")
      body.get(8).text mustBe messages("rejected.notification.guidance.section.3.paragraph.3")
    }

    "contain, on amendment errors, the 'check-your-answers' paragraph" in {
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
