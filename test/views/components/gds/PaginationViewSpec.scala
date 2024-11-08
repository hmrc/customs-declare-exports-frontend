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

package views.components.gds

import base.ExportsTestData
import controllers.routes.SavedDeclarationsController
import models.{ExportsDeclaration, Page, Paginated}
import org.jsoup.nodes.Element
import org.scalatest.Assertion
import play.twirl.api.HtmlFormat.Appendable
import views.html.drafts.pagination
import views.common.UnitViewSpec

class PaginationViewSpec extends UnitViewSpec {

  private def paginationComponent(
    declarations: Seq[ExportsDeclaration] = List.empty,
    elementsTotal: Int = 0,
    currentPage: Int = 1,
    pageSize: Int = 10
  ): Appendable = pagination(Paginated(declarations, Page(currentPage, pageSize), elementsTotal), neighbourPagesAmount = 1)

  private val declaration = ExportsTestData.aDeclaration()

  "Pagination component" should {

    "contain pagination summary" when {

      "there are no elements" in {
        val summary = paginationComponent().getElementsByClass("page-summary")

        summary.first must containMessage("site.pagination.showing.no")
        summary.first must containMessage("draft.declarations.pagination.plural")
      }

      "there is single element" in {
        val summary = paginationComponent(List(declaration), elementsTotal = 1).getElementsByClass("page-summary")

        summary.first must containMessage("site.pagination.showing")
        summary.first must containText("1")
        summary.first must containMessage("draft.declarations.pagination.singular")
      }

      "there are multiple elements" in {
        val summary = paginationComponent(List.fill(10)(declaration), elementsTotal = 30).getElementsByClass("page-summary")

        summary.first must containMessage("site.pagination.showing")
        summary.first must containText("1 – 10")
        summary.first must containMessage("site.pagination.of")
        summary.first must containText("30")
        summary.first must containMessage("draft.declarations.pagination.plural")
      }
    }

    "contain pagination controls" when {

      "there is single page" in {
        val appendable = paginationComponent(List.fill(3)(declaration), elementsTotal = 3)
        appendable.getElementsByClass("govuk-pagination").size mustBe 0
      }

      "there are two pages and the current one is 1" in {
        val appendable = paginationComponent(List.fill(10)(declaration), elementsTotal = 20)
        val pagination = appendable.getElementsByClass("govuk-pagination").first

        pagination.childrenSize mustBe 2

        val pages = pagination.getElementsByClass("govuk-pagination__list").first.children
        pages.size mustBe 2

        pagination.getElementsByClass("govuk-pagination__previous").size mustBe 0

        pagination.getElementsByClass("govuk-pagination__item--current").first.text mustBe "1"
        verify(pages.get(1).child(0), "2", SavedDeclarationsController.displayDeclarations(2).url)

        val next = pagination.getElementsByClass("govuk-pagination__next").first.child(0)
        verify(next, messages("pagination.next"), SavedDeclarationsController.displayDeclarations(2).url)
      }

      "there are two pages and the current one is 2" in {
        val appendable = paginationComponent(List.fill(10)(declaration), elementsTotal = 20, currentPage = 2)
        val pagination = appendable.getElementsByClass("govuk-pagination").first

        pagination.childrenSize mustBe 2

        val previous = pagination.getElementsByClass("govuk-pagination__prev").first.child(0)
        verify(previous, messages("pagination.previous"), SavedDeclarationsController.displayDeclarations().url)

        val pages = pagination.getElementsByClass("govuk-pagination__list").first.children
        pages.size mustBe 2

        verify(pages.get(0).child(0), "1", SavedDeclarationsController.displayDeclarations().url)
        pagination.getElementsByClass("govuk-pagination__item--current").first.text mustBe "2"

        pagination.getElementsByClass("govuk-pagination__next").size mustBe 0
      }

      "there are seven pages and the current one is 4" in {
        val appendable = paginationComponent(List.fill(10)(declaration), elementsTotal = 70, currentPage = 4)
        val pagination = appendable.getElementsByClass("govuk-pagination").first

        pagination.childrenSize mustBe 3

        val previous = pagination.getElementsByClass("govuk-pagination__prev").first.child(0)
        verify(previous, messages("pagination.previous"), SavedDeclarationsController.displayDeclarations(3).url)

        val pages = pagination.getElementsByClass("govuk-pagination__list").first.children
        pages.size mustBe 7

        verify(pages.get(0).child(0), "1", SavedDeclarationsController.displayDeclarations().url)
        verify(pages.get(2).child(0), "3", SavedDeclarationsController.displayDeclarations(3).url)

        pagination.getElementsByClass("govuk-pagination__item--current").first.text mustBe "4"

        verify(pages.get(4).child(0), "5", SavedDeclarationsController.displayDeclarations(5).url)
        verify(pages.get(6).child(0), "7", SavedDeclarationsController.displayDeclarations(7).url)

        val next = pagination.getElementsByClass("govuk-pagination__next").first.child(0)
        verify(next, messages("pagination.next"), SavedDeclarationsController.displayDeclarations(5).url)

        pagination.getElementsByClass("govuk-pagination__item--ellipses").size mustBe 2
      }
    }
  }

  private def verify(element: Element, expectedText: String, expectedUrl: String): Assertion = {
    element.text mustBe expectedText
    element.attr("href") mustBe expectedUrl
  }
}
