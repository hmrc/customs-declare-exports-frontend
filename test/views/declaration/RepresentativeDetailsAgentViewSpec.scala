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

package views.declaration

import base.Injector
import forms.common.YesNoAnswer.YesNoAnswers
import forms.common.{Address, Eori}
import forms.declaration.RepresentativeAgent.form
import forms.declaration.consignor.ConsignorDetails
import forms.declaration.{EntityDetails, IsExs, RepresentativeAgent}
import models.DeclarationType._
import models.declaration.Parties
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestHelper
import tools.Stubs
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.representative_details_agent
import views.tags.ViewTest

@ViewTest
class RepresentativeDetailsAgentViewSpec extends PageWithButtonsSpec with ExportsTestHelper with Stubs with Injector {

  private val page = instanceOf[representative_details_agent]

  override val typeAndViewInstance = (STANDARD, page(form())(_, _))

  private def createView(frm: Form[RepresentativeAgent] = form(), mode: Mode = Normal)(implicit request: JourneyRequest[_]): Document =
    page(frm)(request, messages)

  "Representative Details Agent View on empty page" should {

    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display page title" in {
        view.getElementsByTag("h1").first() must containMessage("declaration.representative.agent.title")
      }

      "display two radio buttons with description (not selected)" in {
        view.getElementsByClass("govuk-radios__item").size mustBe 2

        val optionDirect = view.getElementById("agent_yes")
        optionDirect.attr("checked") mustBe empty

        val optionIndirect = view.getElementById("agent_no")
        optionIndirect.attr("checked") mustBe empty

      }

      val createViewWithMode: Mode => Document = mode => createView(mode = mode)
      checkAllSaveButtonsAreDisplayed(createViewWithMode)
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display 'Back' button that links to 'Exporter Details' page" in {
        val view = createView()

        val backButton = view.getElementById("back-link")

        backButton must containMessage("site.backToPreviousQuestion")
        backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.ExporterDetailsController.displayPage())
      }
    }

    onClearance { implicit request =>
      "display 'Back' button that links to 'Consignor Details' page" in {
        val cachedParties = Parties(
          isExs = Some(IsExs(YesNoAnswers.yes)),
          consignorDetails =
            Some(ConsignorDetails(EntityDetails(None, address = Some(Address("fullName", "addressLine", "townOrCity", "postCode", "country")))))
        )
        val requestWithCachedParties = journeyRequest(request.cacheModel.copy(parties = cachedParties))

        val backButton = createView()(requestWithCachedParties).getElementById("back-link")

        backButton must containMessage("site.backToPreviousQuestion")
        backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.ConsignorDetailsController.displayPage())
      }

      "display 'Back' button that links to 'Consignor Eori Number' page" in {
        val cachedParties = Parties(
          isExs = Some(IsExs(YesNoAnswers.yes)),
          consignorDetails = Some(ConsignorDetails(EntityDetails(eori = Some(Eori("GB1234567890000")), None)))
        )
        val requestWithCachedParties = journeyRequest(request.cacheModel.copy(parties = cachedParties))

        val backButton = createView()(requestWithCachedParties).getElementById("back-link")

        backButton must containMessage("site.backToPreviousQuestion")
        backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.ConsignorEoriNumberController.displayPage())
      }

      "display 'Back' button that links to 'Is Exs' page" in {
        val cachedParties = Parties(isExs = Some(IsExs(YesNoAnswers.no)))
        val requestWithCachedParties = journeyRequest(request.cacheModel.copy(parties = cachedParties))

        val backButton = createView()(requestWithCachedParties).getElementById("back-link")

        backButton must containMessage("site.backToPreviousQuestion")
        backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.IsExsController.displayPage())
      }
    }

    "Representative Details Status View for invalid input" should {
      onEveryDeclarationJourney() { implicit request =>
        "display errors when answer is incorrect" in {
          val view = createView(form().bind(Map("representingAgent" -> "invalid")))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#agent_yes")

          view.getElementsByClass("#govuk-error-message").text() contains messages("choicePage.input.error.empty")
        }
      }
    }
  }

  "Representative Details Status View when filled" should {
    onEveryDeclarationJourney() { implicit request =>
      "display data" in {
        val view = createView(form().bind(Map("representingAgent" -> "Yes")))
        view.getElementById("agent_yes").getElementsByAttribute("checked").size() mustBe 1
      }
    }
  }
}
