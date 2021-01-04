/*
 * Copyright 2021 HM Revenue & Customs
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

import java.time.{ZoneOffset, ZonedDateTime}
import java.util.function.Predicate

import base.Injector
import forms.common.YesNoAnswer.YesNoAnswers.yes
import forms.declaration.{CommodityDetails, WarehouseIdentification}
import models.ExportsDeclaration
import models.declaration.notifications.Notification
import models.declaration.submissions.SubmissionStatus
import org.jsoup.nodes.{Document, Element}
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.submitted_declaration_page

class SubmittedDeclarationPageViewSpec extends UnitViewSpec with Stubs with ExportsTestData with Injector {

  private val notification = Notification("actionId", "mrn", ZonedDateTime.now(ZoneOffset.UTC), SubmissionStatus.ACCEPTED, Seq.empty, "payload")

  val declarationPage = instanceOf[submitted_declaration_page]
  def createView(declaration: ExportsDeclaration = aDeclaration()): Document =
    declarationPage(Seq(notification))(journeyRequest(declaration), messages)

  def links(view: Document) = {
    val allLinks = view.getElementsByClass("govuk-link")

    val filter = new Predicate[Element] {
      override def test(elm: Element): Boolean =
        elm.text().contains("Print") || elm.text().contains("Sign out") || elm.text().contains("page not working")
    }
    allLinks.removeIf(filter)
    allLinks
  }

  "Summary page" should {

    "should display correct title" in {

      createView().getElementById("title").text() mustBe messages("declaration.summary.submitted-header")
    }

    "should display correct back link" in {

      val backButton = createView().getElementById("back-link")

      backButton.text() mustBe messages("site.back")
      backButton must haveHref(controllers.routes.SubmissionsController.displayListOfSubmissions())
    }

    "have references section" in {

      val view = createView()
      view.getElementById("declaration-references-summary").text() mustNot be(empty)

      links(view) mustBe empty
    }

    "not have parties section" in {

      createView().getElementById("declaration-parties-summary") mustBe null
    }

    "have parties section" in {

      val view = createView(declaration = aDeclaration(withExporterDetails()))
      view.getElementById("declaration-parties-summary").text() mustNot be(empty)
      links(view) mustBe empty
    }

    "not have countries section" in {
      createView().getElementById("declaration-countries-summary") mustBe null
    }

    "have countries section" in {

      val view = createView(declaration = aDeclaration(withDestinationCountry()))
      view.getElementById("declaration-countries-summary").text() mustNot be(empty)
      links(view) mustBe empty
    }

    "not have locations section" in {

      createView().getElementById("declaration-locations-summary") mustBe null
    }

    "have locations section with UK office of exit" in {

      val view = createView(declaration = aDeclaration(withOfficeOfExit(officeId = "office-Id", isUkOfficeOfExit = yes)))
      view.getElementById("declaration-locations-summary").text() must include("office-Id")
      links(view) mustBe empty
    }

    "have locations section with office of exit outside UK" in {

      val view = createView(declaration = aDeclaration(withOfficeOfExit(), withOfficeOfExitOutsideUK("office-id-outside-uk")))
      view.getElementById("declaration-locations-summary").text() must include("office-id-outside-uk")
      links(view) mustBe empty
    }

    "not have transaction section" in {

      createView().getElementById("declaration-transaction-summary") mustBe null
    }

    "have transaction section" in {

      val view = createView(declaration = aDeclaration(withNatureOfTransaction("1")))
      view.getElementById("declaration-transaction-summary").text() mustNot be(empty)
      links(view) mustBe empty
    }

    "not have items section" in {

      createView().getElementById("declaration-items-summary") mustBe null
    }

    "have items section" in {

      val details = CommodityDetails(Some("12345678"), Some("Description"))
      val view = createView(declaration = aDeclaration(withItem(anItem(withCommodityDetails(details)))))
      view.getElementById("declaration-items-summary-0").text() mustNot be(empty)
      links(view) mustBe empty
    }

    "not have warehouse section" in {

      createView().getElementById("declaration-warehouse-summary") mustBe null
    }

    "have warehouse section" in {

      val view = createView(declaration = aDeclaration(withWarehouseIdentification(Some(WarehouseIdentification(Some("12345"))))))
      view.getElementById("declaration-warehouse-summary").text() mustNot be(empty)
      links(view) mustBe empty
    }

    "not have transport section" in {

      createView().getElementById("declaration-transport-summary") mustBe null
    }

    "have transport section" in {

      val view = createView(declaration = aDeclaration(withBorderTransport()))
      view.getElementById("declaration-transport-summary").text() mustNot be(empty)
      links(view) mustBe empty
    }
  }
}
