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
import forms.declaration.DeclarationHolder
import models.Mode
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.parties_section_holders_gds

class PartiesSectionHoldersViewSpec extends UnitViewSpec with ExportsTestData with Injector {

  val eori1 = "eori1"
  val eori2 = "eori2"
  val authorisationTypeCode1 = "partyType1"
  val authorisationTypeCode2 = "partyType2"

  val holders =
    Seq(DeclarationHolder(Some(authorisationTypeCode1), Some(Eori(eori1))), DeclarationHolder(Some(authorisationTypeCode2), Some(Eori(eori2))))

  private val section = instanceOf[parties_section_holders_gds]

  "Holders (authorised) parties section" should {

    "display holders with answer no if empty" in {

      val view = section(Mode.Normal, Seq.empty)(messages, journeyRequest())
      val row = view.getElementsByClass("holders-row")

      row must haveSummaryKey(messages("declaration.summary.parties.holders"))
      row must haveSummaryValue(messages("site.no"))

      row must haveSummaryActionsText("site.change declaration.summary.parties.holders.empty.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.DeclarationHolderController.displayPage(Mode.Normal))
    }

    "display holders if exists" in {

      val view = section(Mode.Normal, holders)(messages, journeyRequest())
      val table = view.getElementById("holders-table")

      table.getElementsByTag("caption").text() mustBe messages("declaration.summary.parties.holders")

      table.getElementsByClass("govuk-table__header").get(0).text() mustBe messages("declaration.summary.parties.holders.type")
      table.getElementsByClass("govuk-table__header").get(1).text() mustBe messages("declaration.summary.parties.holders.eori")

      val row1 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(0)
      row1.getElementsByClass("govuk-table__cell").get(0).text() mustBe messages(authorisationTypeCode1)
      row1.getElementsByClass("govuk-table__cell").get(1).text() mustBe messages(eori1)
      val row1ChangeLink = row1.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first()
      row1ChangeLink must haveHref(controllers.declaration.routes.DeclarationHolderController.displayPage())
      row1ChangeLink.text() mustBe messages("site.change declaration.summary.parties.holders.change")

      val row2 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(1)
      row2.getElementsByClass("govuk-table__cell").get(0).text() mustBe messages(authorisationTypeCode2)
      row2.getElementsByClass("govuk-table__cell").get(1).text() mustBe messages(eori2)
      val row2ChangeLink = row2.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first()
      row2ChangeLink must haveHref(controllers.declaration.routes.DeclarationHolderController.displayPage())
      row2ChangeLink.text() mustBe messages("site.change declaration.summary.parties.holders.change")
    }
  }
}
