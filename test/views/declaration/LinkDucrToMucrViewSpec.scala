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
import config.AppConfig
import controllers.declaration.routes
import forms.common.YesNoAnswer
import models.DeclarationType._
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.link_ducr_to_mucr
import views.tags.ViewTest

@ViewTest
class LinkDucrToMucrViewSpec extends UnitViewSpec with CommonMessages with Injector {

  private val appConfig = instanceOf[AppConfig]
  private val page = instanceOf[link_ducr_to_mucr]
  private val form: Form[YesNoAnswer] = YesNoAnswer.form()

  private def createView(form: Form[YesNoAnswer] = form)(implicit request: JourneyRequest[_]): Document =
    page(form)(request, messages)

  "'Link DUCR to MUCR' view" should {

    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display header" in {
        view.getElementById("section-header") must containMessage("declaration.section.1")
      }

      "display page title" in {
        view.getElementsByTag("h1").first() must containMessage("declaration.linkDucrToMucr.title")
      }

      "display paragraph" in {
        view.getElementsByClass("govuk-inset-text").get(0).text mustBe
          messages("declaration.linkDucrToMucr.paragraph").replace("<br><br>", " ")
      }

      "display the notification banner" in {
        val banner = view.getElementsByClass("govuk-notification-banner").first
        banner.child(0) must containMessage("declaration.linkDucrToMucr.banner.title")
        banner.child(1) must containMessage("declaration.linkDucrToMucr.banner.content")
      }

      "display two Yes/No radio buttons" in {
        val radios = view.getElementsByClass("govuk-radios").first.children
        radios.size mustBe 2
        Option(radios.first.getElementById("code_yes")) mustBe defined
        Option(radios.last.getElementById("code_no")) mustBe defined
      }

      "select the 'Yes' radio when clicked" in {
        val form = YesNoAnswer.form().bind(Map("yesNo" -> "Yes"))
        val view = createView(form = form)
        view.getElementById("code_yes") must beSelected
      }

      "select the 'No' radio when clicked" in {
        val form = YesNoAnswer.form().bind(Map("yesNo" -> "No"))
        val view = createView(form = form)
        view.getElementById("code_no") must beSelected
      }

      "display the 'MUCR consolidation' details" in {
        val detailsTitle = view.getElementsByClass("govuk-details__summary-text").first.text
        detailsTitle mustBe messages("declaration.linkDucrToMucr.details")

        val detailsHint1 = view.getElementById("link-ducr-to-mucr-hint1")
        detailsHint1.child(0) must haveHref(appConfig.notesForMucrConsolidationUrl)

        removeBlanksIfAnyBeforeDot(detailsHint1.text) mustBe messages(
          "declaration.linkDucrToMucr.details.hint1",
          messages("declaration.linkDucrToMucr.details.hint1.link")
        )

        view.getElementById("link-ducr-to-mucr-hint2").text mustBe messages("declaration.linkDucrToMucr.details.hint2")
      }
    }

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { implicit request =>
      "display 'Back' button to 'Consignment Reference' page" in {
        val backButton = createView().getElementById("back-link")
        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(routes.LocalReferenceNumberController.displayPage)
      }
    }

    onJourney(SUPPLEMENTARY) { implicit request =>
      "display 'Back' button to 'Consignment Reference' page" in {
        val backButton = createView().getElementById("back-link")
        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(routes.ConsignmentReferencesController.displayPage)
      }
    }
  }
}
