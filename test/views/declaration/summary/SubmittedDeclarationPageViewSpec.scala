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

package views.declaration.summary

import base.Injector
import forms.declaration.CommodityDetails
import models.DeclarationType._
import models.ExportsDeclaration
import org.jsoup.nodes.{Document, Element}
import services.cache.ExportsTestHelper
import testdata.SubmissionsTestData.submission
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.submitted_declaration_page

import java.util.function.Predicate

class SubmittedDeclarationPageViewSpec extends UnitViewSpec with Stubs with ExportsTestHelper with Injector {

  val declarationPage = instanceOf[submitted_declaration_page]
  def createView(declaration: ExportsDeclaration = aDeclaration()): Document =
    declarationPage(Some(submission), declaration)(request, messages)

  def links(view: Document) = {
    val allLinks = view.getElementsByClass("govuk-link")

    val filter = new Predicate[Element] {
      override def test(elm: Element): Boolean =
        elm.text().contains("Print") || elm.text().contains("feedback") ||
          elm.text().contains("Sign out") || elm.text().contains("page not working")
    }
    allLinks.removeIf(filter)
    allLinks
  }

  "SubmittedDeclarationPageView" should {

    "display correct title" in {
      createView().getElementById("title").text() mustBe messages("declaration.summary.submitted-header")
    }

    "display correct back link" in {
      val backButton = createView(aDeclaration(withId("declaration-id"))).getElementById("back-link")

      backButton.text() mustBe messages("site.back")
      backButton must haveHref(controllers.routes.DeclarationDetailsController.displayPage("declaration-id"))
    }

    "have references section" in {
      val view = createView()
      view.getElementById("declaration-references-summary").text() mustNot be(empty)

      links(view) mustBe empty
    }

    "not have parties section" in {
      Option(createView().getElementById("declaration-parties-summary")) mustBe None
    }

    "have parties section" in {
      val view = createView(declaration = aDeclaration(withExporterDetails()))
      view.getElementById("declaration-parties-summary").text() mustNot be(empty)
      links(view) mustBe empty
    }

    "not have countries section" in {
      Option(createView().getElementById("declaration-countries-summary")) mustBe None
    }

    "have countries section" in {
      val view = createView(declaration = aDeclaration(withDestinationCountry()))
      view.getElementById("declaration-countries-summary").text() mustNot be(empty)
      links(view) mustBe empty
    }

    "not have locations section" in {
      Option(createView().getElementById("declaration-locations-summary")) mustBe None
    }

    "have locations section with UK office of exit" in {
      val view = createView(declaration = aDeclaration(withOfficeOfExit(officeId = "office-Id")))
      view.getElementById("declaration-locations-summary").text() must include("office-Id")
      links(view) mustBe empty
    }

    for (decType <- List(CLEARANCE, SIMPLIFIED, OCCASIONAL))
      yield s"not have transaction section in $decType declaration" in {
        val view = createView(declaration = aDeclaration(withType(decType)))
        Option(view.getElementById("declaration-transaction-summary")) mustBe None
      }

    "have transaction section" in {
      val view = createView(declaration = aDeclaration(withNatureOfTransaction("1")))
      view.getElementById("declaration-transaction-summary").text() mustNot be(empty)
      links(view) mustBe empty
    }

    "not have items section" in {
      Option(createView().getElementById("declaration-items-summary")) mustBe None
    }

    "have items section" in {
      val details = CommodityDetails(Some("1234567890"), Some("Description"))
      val view = createView(declaration = aDeclaration(withItem(anItem(withCommodityDetails(details)))))
      view.getElementById("declaration-items-summary-0").text() mustNot be(empty)
      links(view) mustBe empty
    }

    "not have transport section" in {
      Option(createView().getElementById("declaration-transport-summary")) mustBe None
    }

    "have transport section" in {
      val view = createView(declaration = aDeclaration(withBorderTransport()))
      view.getElementById("declaration-transport-summary").text() mustNot be(empty)
      links(view) mustBe empty
    }
  }
}
