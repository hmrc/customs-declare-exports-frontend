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

package views.declaration

import base.Injector
import base.TestHelper.createRandomAlphanumericString
import config.AppConfig
import controllers.declaration.routes
import forms.declaration.Mucr
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.api.data.Form
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.mucr_code
import views.tags.ViewTest

@ViewTest
class MucrViewSpec extends UnitViewSpec with CommonMessages with Injector {

  private val appConfig = instanceOf[AppConfig]
  private val page = instanceOf[mucr_code]
  private val form: Form[Mucr] = Mucr.form

  private def createView(form: Form[Mucr] = form)(implicit request: JourneyRequest[_]): Document =
    page(form)(request, messages)

  private val tooLongMucr = createRandomAlphanumericString(36)

  "'Enter the MUCR' view" should {

    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display 'Back' button to 'Link DUCR to MUCR' page" in {
        val backButton = view.getElementById("back-link")
        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(routes.LinkDucrToMucrController.displayPage)
      }

      "display header" in {
        view.getElementById("section-header") must containMessage("declaration.section.1")
      }

      "display page title" in {
        view.getElementsByTag("h1").first() must containMessage("declaration.mucr.title")
      }

      "display the body paragraph" in {
        val para = view.getElementsByClass("govuk-body").first
        para.child(0) must haveHref(appConfig.notesForMucrConsolidationUrl)

        removeBlanksIfAnyBeforeDot(para.text) mustBe messages("declaration.mucr.paragraph", messages("declaration.mucr.paragraph.link"))
      }

      "display the input field where to enter the MUCR" in {
        val inputField = view.getElementsByTag("input")
        inputField.size mustBe 2 // First is the CSRF token
        inputField.last.id mustBe "MUCR"
      }

      "display error for empty MUCR" in {
        verifyError("", "empty")
      }

      "display error for MUCR too long" in {
        verifyError(tooLongMucr, "length")
      }

      "display error for MUCR containing non-allowed characters" in {
        verifyError("CXZY123-#@", "invalid")
      }

      def verifyError(mucr: String, errorKey: String): Assertion = {
        val form = Mucr.form.fillAndValidate(Mucr(mucr))
        val view = createView(form = form)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#MUCR")

        view must containErrorElementWithMessageKey(s"declaration.mucr.error.$errorKey")
      }
    }

    "display the expected tariff details" when {

      onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, SUPPLEMENTARY) { implicit request =>
        val view = createView()
        val tariffTitle = view.getElementsByClass("govuk-details__summary-text")
        tariffTitle.first must containMessage("tariff.expander.title.common")

        val tariffDetails = view.getElementsByClass("govuk-details__text").first
        removeBlanksIfAnyBeforeDot(tariffDetails.text) mustBe messages(
          "tariff.declaration.mucr.common.text",
          messages("tariff.declaration.mucr.common.linkText.0")
        )
      }

      onJourney(CLEARANCE) { implicit request =>
        val view = createView()
        val tariffTitle = view.getElementsByClass("govuk-details__summary-text")
        tariffTitle.first must containMessage("tariff.expander.title.clearance")

        val tariffDetails = view.getElementsByClass("govuk-details__text").first
        removeBlanksIfAnyBeforeDot(tariffDetails.text) mustBe messages(
          "tariff.declaration.mucr.common.text",
          messages("tariff.declaration.mucr.common.linkText.0")
        )
      }
    }
  }
}
