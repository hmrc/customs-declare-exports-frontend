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
import play.api.data.FormError
import play.api.mvc.Call
import play.twirl.api.HtmlFormat.Appendable
import tools.Stubs
import views.declaration.spec.UnitViewSpec

trait SummaryViewSpec extends UnitViewSpec with Injector with Stubs {

  val dummyFormError = Seq(FormError("dummy", "error.unknown"))
  private val backLink = Call("GET", "/backLink")

  def commonBehaviour(state: String, view: Appendable): Unit =
    s"declaration in $state state" should {

      "have common behaviours such as" when {
        "have references section" in {
          view.getElementsByTag("h2").first.text mustBe messages(s"declaration.summary.heading")
        }

        "display Exit and come back later button" in {
          view.getElementById("exit-and-complete-later").text mustBe messages(exitAndReturnCaption)
        }

        "should display correct title" in {
          val correctTitle = state match {
            case "errors" => "declaration.summary.amend-header"
            case "ready"  => "declaration.summary.normal-header"
            case "draft"  => "declaration.summary.saved-header"
          }
          view.getElementById("title").text() mustBe messages(correctTitle)
        }

        "should display correct back link" in {
          val backButton = view.getElementById("back-link")

          backButton.text() mustBe messages("site.backToDeclarations")
          backButton must haveHref(backLink.url)
        }

        "warning text should be displayed" in {
          val warningText = s"! ${messages("site.warning")} ${messages("declaration.summary.warning")}"
          view.getElementsByClass("govuk-warning-text").text mustBe warningText
        }
      }
    }

  def displayErrorSummary(view: Appendable): Unit =
    "error summary should be displayed" in {
      val errorSummary = view.getElementsByClass("govuk-error-summary")
      errorSummary.text contains messages("error.unknown")
      errorSummary.size mustBe dummyFormError.length
    }

  // scalastyle:off
  def sectionsVisibility(view: ExportsDeclaration => Appendable): Unit = {
    "not have parties section" in {
      view(aDeclaration()).getElementsByClass("parties-card").size mustBe 0
    }

    "have parties section" in {
      view(aDeclaration(withExporterDetails())).getElementsByClass("parties-card").text mustNot be(empty)
    }

    "not have 'Routes and Locations' section" in {
      view(aDeclaration()).getElementsByClass("routes-and-locations-card").size mustBe 0
    }

    "have 'Routes and Locations' section" in {
      val declaration = aDeclaration(withDestinationCountry(), withOfficeOfExit(officeId = "office-Id"))
      view(declaration).getElementsByClass("routes-and-locations-card").text mustNot be(empty)
    }

    for (declarationType <- List(CLEARANCE, SIMPLIFIED, OCCASIONAL))
      yield s"not have transaction section in $declarationType declaration" in {
        view(aDeclaration(withType(declarationType))).getElementsByClass("transaction-card").size mustBe 0
      }

    "have transaction section" in {
      view(aDeclaration(withNatureOfTransaction("1"))).getElementsByClass("transaction-card").text mustNot be(empty)
    }

    "not have items section" in {
      view(aDeclaration()).getElementsByClass("items-card").size mustBe 0
    }

    "have items section" in {
      val details = CommodityDetails(Some("1234567890"), Some("Description"))
      view(aDeclaration(withItem(anItem(withCommodityDetails(details)))))
        .getElementsByClass("items-card")
        .text mustNot be(empty)
    }

    "not have transport section" in {
      view(aDeclaration()).getElementsByClass("transport-card") mustBe empty
    }

    "have transport section" in {
      view(aDeclaration(withBorderTransport())).getElementsByClass("transport-card").text mustNot be(empty)
    }
  }
  // scalastyle:on
}
