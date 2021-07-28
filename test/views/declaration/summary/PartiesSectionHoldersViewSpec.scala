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

import base.OverridableInjector
import forms.common.Eori
import forms.declaration.declarationHolder.DeclarationHolderAdd
import models.{ExportsDeclaration, Mode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.api.mvc.Call
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.parties_section_holders

class PartiesSectionHoldersViewSpec extends UnitViewSpec with ExportsTestData with BeforeAndAfterEach {

  val eori1 = "eori1"
  val eori2 = "eori2"
  val authorisationTypeCode1 = "ACE"
  val authorisationTypeCode2 = "AWB"

  val holders =
    Seq(DeclarationHolderAdd(Some(authorisationTypeCode1), Some(Eori(eori1))), DeclarationHolderAdd(Some(authorisationTypeCode2), Some(Eori(eori2))))

  private val partiesSectionHoldersHelper = mock[PartiesSectionHoldersHelper]
  private val testCall = Call("GET", "/test-call-url")

  private val section = new OverridableInjector(bind[PartiesSectionHoldersHelper].to(partiesSectionHoldersHelper)).instanceOf[parties_section_holders]

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(partiesSectionHoldersHelper)
    when(partiesSectionHoldersHelper.changeLinkTarget(any[ExportsDeclaration])).thenReturn(_ => testCall)
  }

  "Holders (authorised) parties section" when {

    "DeclarationHoldersData is empty" should {

      "display holders with answer 'No'" in {

        val view = section(Mode.Normal, aDeclaration())(messages)
        val row = view.getElementsByClass("holders-row")

        row must haveSummaryKey(messages("declaration.summary.parties.holders"))
        row must haveSummaryValue(messages("site.no"))

        row must haveSummaryActionsTexts("site.change", "declaration.summary.parties.holders.empty.change")

        row must haveSummaryActionsHref(testCall)
      }
    }

    "DeclarationHoldersData is NOT empty" should {

      "display holders" in {

        val view = section(Mode.Normal, aDeclaration(withDeclarationHolders(holders: _*)))(messages)
        val table = view.getElementById("holders-table")

        table.getElementsByTag("caption").text() mustBe messages("declaration.summary.parties.holders")

        table.getElementsByClass("govuk-table__header").get(0).text() mustBe messages("declaration.summary.parties.holders.type")
        table.getElementsByClass("govuk-table__header").get(1).text() mustBe messages("declaration.summary.parties.holders.eori")
        table.getElementsByClass("govuk-table__header").get(2).text() mustBe messages("site.change.header")

        val row1 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(0)
        row1.getElementsByClass("govuk-table__cell").get(0).text() must include(authorisationTypeCode1)
        row1.getElementsByClass("govuk-table__cell").get(1).text() mustBe eori1
        val row1ChangeLink = row1.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first()
        row1ChangeLink must haveHref(testCall)
        row1ChangeLink
          .text() mustBe s"${messages("site.change")} ${messages("declaration.summary.parties.holders.change", authorisationTypeCode1, eori1)}"

        val row2 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(1)
        row2.getElementsByClass("govuk-table__cell").get(0).text() must include(authorisationTypeCode2)
        row2.getElementsByClass("govuk-table__cell").get(1).text() mustBe eori2
        val row2ChangeLink = row2.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first()
        row2ChangeLink must haveHref(testCall)
        row2ChangeLink
          .text() mustBe s"${messages("site.change")} ${messages("declaration.summary.parties.holders.change", authorisationTypeCode2, eori2)}"
      }
    }
  }
}
