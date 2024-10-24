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

package views.section2

import base.ExportsTestData.eori
import base.Injector
import controllers.section2.routes._
import forms.common.{Address, Eori, YesNoAnswer}
import models.DeclarationType._
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable
import views.common.UnitViewSpec
import views.html.section2.third_party_goods_transportation

class ThirdPartyGoodsTransportationViewSpec extends UnitViewSpec with Injector {

  private val page = instanceOf[third_party_goods_transportation]
  private val form: Form[YesNoAnswer] = YesNoAnswer.form()

  private def createView(form: Form[YesNoAnswer] = form)(implicit request: JourneyRequest[_]): Appendable =
    page(form)(request, messages)

  "ThirdPartyGoodsTransportation view" should {

    checkMessages(
      "declaration.thirdPartyGoodsTransportation.title",
      "declaration.thirdPartyGoodsTransportation.body",
      "declaration.thirdPartyGoodsTransportation.radio.yes",
      "declaration.thirdPartyGoodsTransportation.radio.no",
      "declaration.thirdPartyGoodsTransportation.radio.text",
      "declaration.thirdPartyGoodsTransportation.radio.error"
    )

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { implicit request =>
      val view = createView()

      "display page title" in {
        view.getElementsByTag("h1") must containMessageForElements("declaration.thirdPartyGoodsTransportation.title")
      }

      "display same page title and header" in {
        view.title() must include(view.getElementsByTag("h1").text())
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.2")
      }

      "display paragraph" in {
        view.getElementsByClass("govuk-body").first() must containMessage("declaration.thirdPartyGoodsTransportation.body")
      }

      "display radio buttons" in {
        view.getElementsByAttributeValue("for", "code_yes") must containMessageForElements("declaration.thirdPartyGoodsTransportation.radio.yes")
        view.getElementsByAttributeValue("for", "code_no") must containMessageForElements("declaration.thirdPartyGoodsTransportation.radio.no")
        view.getElementById("code_no-item-hint") must containMessage("declaration.thirdPartyGoodsTransportation.radio.text", eori)
      }

      "display error when all entered input is incorrect" in {
        val error = "declaration.thirdPartyGoodsTransportation.radio.error"
        val formWithError = form.withError("code_yes", error)
        val view: Document = createView(formWithError)(request)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#code_yes")
      }

      "display existing input data" in {
        val formWithData = form.fill(YesNoAnswer.Yes.get)
        val view = createView(formWithData)(request)

        view.getElementById("code_yes").hasAttr("checked") mustBe true
      }

      checkAllSaveButtonsAreDisplayed(createView())
    }

    onJourney(Seq(STANDARD, SIMPLIFIED, OCCASIONAL), withDeclarantIsExporter(YesNoAnswer.YesNoAnswers.yes)) { implicit request =>
      "display 'Back' button to /are-you-the-exporter" in {
        val backButton = createView().getElementById("back-link")
        backButton must containMessage("site.backToPreviousQuestion")
        backButton.getElementById("back-link") must haveHref(DeclarantExporterController.displayPage)
      }
    }

    onJourney(Seq(STANDARD, SIMPLIFIED, OCCASIONAL), withDeclarantIsExporter(YesNoAnswer.YesNoAnswers.no)) { implicit request =>
      "display 'Back' button to /representation-type-agreed" in {
        val backButton = createView().getElementById("back-link")
        backButton must containMessage("site.backToPreviousQuestion")
        backButton.getElementById("back-link") must haveHref(RepresentativeStatusController.displayPage)
      }
    }

    onJourney(Seq(CLEARANCE), withConsignorDetails(Some(Eori("12345")), None)) { implicit request =>
      "display 'Back' button to Consignor Eori" in {
        val backButton = createView().getElementById("back-link")
        backButton must containMessage("site.backToPreviousQuestion")
        backButton.getElementById("back-link") must haveHref(ConsignorEoriNumberController.displayPage)
      }
    }

    onJourney(Seq(CLEARANCE), withConsignorDetails(None, Some(Address("1", "2", "3", "4", "5")))) { implicit request =>
      "display 'Back' button to Consignor address" in {
        val backButton = createView().getElementById("back-link")
        backButton must containMessage("site.backToPreviousQuestion")
        backButton.getElementById("back-link") must haveHref(ConsignorDetailsController.displayPage)
      }
    }
  }
}
