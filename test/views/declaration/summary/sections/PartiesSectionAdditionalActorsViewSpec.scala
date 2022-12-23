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

package views.declaration.summary.sections

import base.Injector
import controllers.declaration.routes.{AdditionalActorsAddController, AdditionalActorsSummaryController}
import forms.common.Eori
import forms.declaration.DeclarationAdditionalActors
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.sections.parties_section_additional_actors

class PartiesSectionAdditionalActorsViewSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  val eori1 = "eori1"
  val partyType1 = "CS"

  val eori2 = "eori2"
  val partyType2 = "MF"

  val eori3 = "eori3"
  val partyType3 = "WH"

  val prefix = "declaration.summary.parties.additional"

  val additionalActors = List(
    DeclarationAdditionalActors(Some(Eori(eori1)), Some(partyType1)),
    DeclarationAdditionalActors(Some(Eori(eori2)), Some(partyType2)),
    DeclarationAdditionalActors(Some(Eori(eori3)), Some(partyType3))
  )

  private val section = instanceOf[parties_section_additional_actors]

  "Additional actors parties section" should {

    "display additional actors with answer no if empty" in {
      val view = section(Seq.empty)(messages)
      val row = view.getElementsByClass("additionalActors-row")

      row must haveSummaryKey(messages("declaration.summary.parties.additional"))
      row must haveSummaryValue(messages("site.no"))

      row must haveSummaryActionsTexts("site.change", "declaration.summary.parties.additional.empty.change")
      row must haveSummaryActionWithPlaceholder(AdditionalActorsAddController.displayPage)
    }

    "display additional actors if exists" in {
      val view = section(additionalActors)(messages)
      val table = view.getElementById("additionalActors-table")

      table.getElementsByTag("caption").text mustBe messages("declaration.summary.parties.additional")

      table.getElementsByClass("govuk-table__header").get(0).text mustBe messages("declaration.additionalActors.partyType")
      table.getElementsByClass("govuk-table__header").get(1).text mustBe messages("declaration.additionalActors.eori")
      table.getElementsByClass("govuk-table__header").get(2).text mustBe messages("site.change.header")

      val row1 = table.getElementsByClass("govuk-table__body").first.getElementsByClass("govuk-table__row").get(0)
      row1.getElementsByClass("govuk-table__cell").get(0).text mustBe messages(s"$prefix.CS")
      row1.getElementsByClass("govuk-table__cell").get(1).text mustBe eori1
      val row1ChangeLink = row1.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first
      row1ChangeLink must haveHrefWithPlaceholder(AdditionalActorsSummaryController.displayPage)
      row1ChangeLink must containMessage("site.change")
      row1ChangeLink must containMessage("declaration.summary.parties.additional.change", messages(s"$prefix.CS"), eori1)

      val row2 = table.getElementsByClass("govuk-table__body").first.getElementsByClass("govuk-table__row").get(1)
      row2.getElementsByClass("govuk-table__cell").get(0).text mustBe messages(s"$prefix.MF")
      row2.getElementsByClass("govuk-table__cell").get(1).text mustBe eori2
      val row2ChangeLink = row2.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first
      row2ChangeLink must haveHrefWithPlaceholder(AdditionalActorsSummaryController.displayPage)
      row2ChangeLink must containMessage("site.change")
      row2ChangeLink must containMessage("declaration.summary.parties.additional.change", messages(s"$prefix.MF"), eori2)

      val row3 = table.getElementsByClass("govuk-table__body").first.getElementsByClass("govuk-table__row").get(2)
      row3.getElementsByClass("govuk-table__cell").get(0).text mustBe messages(s"$prefix.WH")
      row3.getElementsByClass("govuk-table__cell").get(1).text mustBe eori3
      val row3ChangeLink = row3.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first
      row3ChangeLink must haveHrefWithPlaceholder(AdditionalActorsSummaryController.displayPage)
      row3ChangeLink must containMessage("site.change")
      row3ChangeLink must containMessage("declaration.summary.parties.additional.change", messages(s"$prefix.WH"), eori3)
    }
  }
}
