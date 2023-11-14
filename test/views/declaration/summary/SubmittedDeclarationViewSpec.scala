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

package views.declaration.summary

import base.Injector
import forms.declaration.CommodityDetails
import models.DeclarationType._
import models.ExportsDeclaration
import models.requests.SessionHelper
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import play.twirl.api.HtmlFormat.Appendable
import services.cache.ExportsTestHelper
import testdata.SubmissionsTestData.submission
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.submitted_declaration_page

import java.util.function.Predicate

class SubmittedDeclarationViewSpec extends UnitViewSpec with Stubs with ExportsTestHelper with Injector {

  val declarationPage = instanceOf[submitted_declaration_page]
  def createView(declaration: ExportsDeclaration = aDeclaration()): Appendable =
    declarationPage(submission, declaration)(journeyRequest(declaration, (SessionHelper.declarationUuid, "decId")), messages)

  def links(view: Appendable): Elements = {
    val allLinks = view.getElementsByClass("govuk-link")

    val filter = new Predicate[Element] {
      override def test(elm: Element): Boolean =
        elm.text.contains("Print") || elm.text.contains("feedback") ||
          elm.text.contains("Sign out") || elm.text.contains("page not working") ||
          elm.text.contains("View declaration summary") || elm.attr("href").contains("/language/")
    }
    allLinks.removeIf(filter)
    allLinks
  }

  "SubmittedDeclarationPageView" should {

    "display correct title" in {
      createView().getElementById("title").text mustBe messages("declaration.summary.submitted-header")
    }

    "display correct back link" in {
      val backButton = createView(aDeclaration(withId("declaration-id"))).getElementById("back-link")

      backButton.text mustBe messages("site.back")
      backButton must haveHref(controllers.routes.DeclarationDetailsController.displayPage(submission.uuid))
    }

    "not have View declaration summary link" in {
      Option(createView().getElementById("view_declaration_summary")) mustBe None
    }

    "have the expected 'Print page' buttons" in {
      val buttons = createView().getElementsByClass("ceds-print-link")
      buttons.size mustBe 2
      buttons.get(0).text mustBe messages("site.print")
      buttons.get(1).text mustBe messages("site.print")
    }

    "have references section" in {
      val view = createView()
      view.getElementsByTag("h2").first.text mustBe messages(s"declaration.summary.heading")

      links(view) mustBe empty
    }

    "not have parties section" in {
      Option(createView().getElementById("declaration-parties-summary")) mustBe None
    }

    "have parties section" in {
      val view = createView(declaration = aDeclaration(withExporterDetails()))
      view.getElementsByClass("parties-card").text mustNot be(empty)
      links(view) mustBe empty
    }

    "not have 'Routes and Locations' section" in {
      createView().getElementsByClass("routes-and-locations-card").size mustBe 0
    }

    "have 'Routes and Locations' section" in {
      val declaration = aDeclaration(withDestinationCountry(), withOfficeOfExit(officeId = "office-Id"))
      val view = createView(declaration)
      view.getElementsByClass("routes-and-locations-card").text mustNot be(empty)
      links(view) mustBe empty
    }

    for (declarationType <- List(CLEARANCE, SIMPLIFIED, OCCASIONAL))
      yield s"not have transaction section in $declarationType declaration" in {
        createView(aDeclaration(withType(declarationType))).getElementsByClass("transaction-card").size mustBe 0
      }

    "have transaction section" in {
      val view = createView(declaration = aDeclaration(withNatureOfTransaction("1")))
      view.getElementsByClass("transaction-card").text mustNot be(empty)
      links(view) mustBe empty
    }

    "not have items section" in {
      createView().getElementsByClass("items-card").size mustBe 0
    }

    "have items section" in {
      val details = CommodityDetails(Some("1234567890"), Some("Description"))
      val view = createView(declaration = aDeclaration(withItem(anItem(withCommodityDetails(details)))))
      view.getElementsByClass("items-card").text mustNot be(empty)
      links(view) mustBe empty
    }

    "not have transport section" in {
      Option(createView().getElementById("declaration-transport-summary")) mustBe None
    }

    "have transport section" in {
      val view = createView(declaration = aDeclaration(withBorderTransport()))
      view.getElementsByClass("transport-card").text mustNot be(empty)
      links(view) mustBe empty
    }
  }
}
