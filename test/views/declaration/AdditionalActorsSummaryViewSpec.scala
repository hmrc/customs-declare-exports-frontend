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
import forms.common.{Eori, YesNoAnswer}
import forms.declaration.DeclarationAdditionalActors
import models.DeclarationType.{OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestHelper
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.additionalActors.additional_actors_summary
import views.tags.ViewTest

@ViewTest
class AdditionalActorsSummaryViewSpec extends UnitViewSpec with ExportsTestHelper with Stubs with Injector {

  val additionalActor1 = DeclarationAdditionalActors(Some(Eori("GB56523343784324")), Some("CS"))
  val additionalActor2 = DeclarationAdditionalActors(Some(Eori("GB56523399999999")), Some("MF"))
  private val page = instanceOf[additional_actors_summary]

  private def createView(
    mode: Mode = Mode.Normal,
    form: Form[YesNoAnswer] = YesNoAnswer.form(),
    actors: Seq[DeclarationAdditionalActors] = Seq.empty
  )(implicit request: JourneyRequest[_]): Document = page(mode, form, actors)

  "have proper messages for labels" in {
    messages must haveTranslationFor("declaration.additionalActors.table.heading")
    messages must haveTranslationFor("declaration.additionalActors.table.multiple.heading")
    messages must haveTranslationFor("declaration.additionalActors.table.party")
    messages must haveTranslationFor("declaration.additionalActors.table.eori")
    messages must haveTranslationFor("declaration.additionalActors.add.another")
  }

  "AdditionalActors Summary View back link" should {
    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display back link" in {
        val view = createView()
        view must containElementWithID("back-link")
        view.getElementById("back-link") must haveHref(controllers.declaration.routes.ConsigneeDetailsController.displayPage(Mode.Normal))
      }
    }
  }

  "AdditionalActors Summary View on empty page" should {
    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      val view = createView()

      "display page title" in {
        view.getElementsByTag("h1").text() mustBe messages("declaration.additionalActors.table.multiple.heading", "0")
      }

      "display page title for multiple items" in {
        createView(actors = Seq.empty).getElementsByTag("h1").text() mustBe messages("declaration.additionalActors.table.multiple.heading", "0")
      }

      "display section header" in {
        view.getElementById("section-header").text() must include(messages("declaration.section.2"))
      }

      val createViewWithMode: Mode => Document = mode => createView(mode = mode)
      checkAllSaveButtonsAreDisplayed(createViewWithMode)
    }
  }

  "DeclarationHolder Summary View when filled" should {

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display one row with data in table" in {

        val view = createView(actors = Seq(additionalActor1))

        // check table header
        view.select("table>thead>tr>th:nth-child(1)").text() mustBe messages("declaration.additionalActors.table.party")
        view.select("table>thead>tr>th:nth-child(2)").text() mustBe messages("declaration.additionalActors.table.eori")
        view.select("table>thead>tr>th:nth-child(3)").text() mustBe messages("site.remove.header")

        // check row
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(1)").text() mustBe messages("declaration.partyType.CS")
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(2)").text() mustBe "GB56523343784324"

        val removeLink = view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(3)").get(0)
        removeLink must containMessage("site.remove")
        removeLink must containMessage(
          "declaration.additionalActors.table.remove.hint",
          messages(s"declaration.partyType.${additionalActor1.partyType.get}"),
          additionalActor1.eori.get.value
        )
      }

      "display two rows with data in table" in {

        val view = createView(actors = Seq(additionalActor1, additionalActor2))

        // check table header
        view.select("table>thead>tr>th:nth-child(1)").text() mustBe messages("declaration.additionalActors.table.party")
        view.select("table>thead>tr>th:nth-child(2)").text() mustBe messages("declaration.additionalActors.table.eori")
        view.select("table>thead>tr>th:nth-child(3)").text() mustBe messages("site.remove.header")

        // check rows
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(1)").text() mustBe messages("declaration.partyType.CS")
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(2)").text() mustBe "GB56523343784324"

        val removeLink1 = view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(3)").get(0)
        removeLink1 must containMessage("site.remove")
        removeLink1 must containMessage(
          "declaration.additionalActors.table.remove.hint",
          messages(s"declaration.partyType.${additionalActor1.partyType.get}"),
          additionalActor1.eori.get.value
        )

        view.select(".govuk-table__body > tr:nth-child(2) > td:nth-child(1)").text() mustBe messages("declaration.partyType.MF")
        view.select(".govuk-table__body > tr:nth-child(2) > td:nth-child(2)").text() mustBe "GB56523399999999"

        val removeLink2 = view.select(".govuk-table__body > tr:nth-child(2) > td:nth-child(3)").get(0)
        removeLink2 must containMessage("site.remove")
        removeLink2 must containMessage(
          "declaration.additionalActors.table.remove.hint",
          messages(s"declaration.partyType.${additionalActor2.partyType.get}"),
          additionalActor2.eori.get.value
        )
      }
    }
  }
}
