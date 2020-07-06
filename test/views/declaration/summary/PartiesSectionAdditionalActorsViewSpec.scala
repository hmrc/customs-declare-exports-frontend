/*
 * Copyright 2020 HM Revenue & Customs
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
import forms.common.Eori
import forms.declaration.DeclarationAdditionalActors
import models.Mode
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.parties_section_additional_actors

class PartiesSectionAdditionalActorsViewSpec extends UnitViewSpec with ExportsTestData with Injector {

  val eori1 = "eori1"
  val partyType1 = "CS"
  val eori2 = "eori2"
  val partyType2 = "MF"

  val additionalActors =
    Seq(DeclarationAdditionalActors(Some(Eori(eori1)), Some(partyType1)), DeclarationAdditionalActors(Some(Eori(eori2)), Some(partyType2)))

  private val section = instanceOf[parties_section_additional_actors]

  "Additional actors parties section" should {

    "display additional actors with answer no if empty" in {

      val view = section(Mode.Normal, Seq.empty)(messages, journeyRequest())
      val row = view.getElementsByClass("additionalActors-row")

      row must haveSummaryKey(messages("declaration.summary.parties.additional"))
      row must haveSummaryValue(messages("site.no"))

      row must haveSummaryActionsText("site.change declaration.summary.parties.additional.empty.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.AdditionalActorsAddController.displayPage(Mode.Normal))
    }

    "display additional actors if exists" in {

      val view = section(Mode.Normal, additionalActors)(messages, journeyRequest())
      val table = view.getElementById("additionalActors-table")

      table.getElementsByTag("caption").text() mustBe messages("declaration.summary.parties.additional")

      table.getElementsByClass("govuk-table__header").get(0).text() mustBe messages("declaration.additionalActors.partyType")
      table.getElementsByClass("govuk-table__header").get(1).text() mustBe messages("declaration.additionalActors.eori")

      val row1 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(0)
      row1.getElementsByClass("govuk-table__cell").get(0).text() mustBe messages("declaration.partyType.CS")
      row1.getElementsByClass("govuk-table__cell").get(1).text() mustBe messages(eori1)
      val row1ChangeLink = row1.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first()
      row1ChangeLink must haveHref(controllers.declaration.routes.AdditionalActorsSummaryController.displayPage())
      row1ChangeLink.text() mustBe s"${messages("site.change")} ${messages("declaration.summary.parties.additional.change")}"

      val row2 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(1)
      row2.getElementsByClass("govuk-table__cell").get(0).text() mustBe messages("declaration.partyType.MF")
      row2.getElementsByClass("govuk-table__cell").get(1).text() mustBe messages(eori2)
      val row2ChangeLink = row2.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first()
      row2ChangeLink must haveHref(controllers.declaration.routes.AdditionalActorsSummaryController.displayPage())
      row2ChangeLink.text() mustBe s"${messages("site.change")} ${messages("declaration.summary.parties.additional.change")}"
    }

  }
}
