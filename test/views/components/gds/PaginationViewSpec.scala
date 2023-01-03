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

package views.components.gds

import base.ExportsTestData
import models.{ExportsDeclaration, Page, Paginated}
import views.declaration.spec.UnitViewSpec
import views.html.declarations.pagination

class PaginationViewSpec extends UnitViewSpec {

  private def paginationComponent(
    declarations: Seq[ExportsDeclaration] = Seq.empty,
    currentPage: Int = 1,
    pageSize: Int = 10,
    elementsTotal: Int = 0
  ) = pagination(
    singularName = messages("saved.declarations.pagination.singular"),
    pluralName = messages("saved.declarations.pagination.plural"),
    pager = Paginated(declarations, Page(currentPage, pageSize), elementsTotal),
    onChange = page => controllers.routes.SavedDeclarationsController.displayDeclarations(page),
    neighbourPagesAmount = 1
  )

  private val declaration = ExportsTestData.aDeclaration()

  "Pagination component" should {

    "contain pagination summary" when {

      "there are no elements" in {
        val summary = paginationComponent().getElementsByClass("ceds-pagination__summary")

        summary.first() must containMessage("site.pagination.showing.no")
        summary.first() must containMessage("saved.declarations.pagination.plural")
      }

      "there is single element" in {
        val summary = paginationComponent(declarations = Seq(declaration), elementsTotal = 1).getElementsByClass("ceds-pagination__summary")

        summary.first() must containMessage("site.pagination.showing")
        summary.first() must containText("1")
        summary.first() must containMessage("saved.declarations.pagination.singular")
      }

      "there are multiple elements" in {
        val summary = paginationComponent(declarations = Seq.fill(10)(declaration), elementsTotal = 30).getElementsByClass("ceds-pagination__summary")

        summary.first() must containMessage("site.pagination.showing")
        summary.first() must containText("1 – 10")
        summary.first() must containMessage("site.pagination.of")
        summary.first() must containText("30")
        summary.first() must containMessage("saved.declarations.pagination.plural")
      }
    }

    "contain pagination controls" when {

      "there is single page" in {
        val controls = paginationComponent(declarations = Seq.fill(3)(declaration), elementsTotal = 3)

        controls.getElementsByClass("ceds-pagination__item").size() mustBe 0
      }

      "there are two pages and the current one is 1" in {
        val controls = paginationComponent(declarations = Seq.fill(10)(declaration), currentPage = 1, elementsTotal = 20)

        controls.getElementsByClass("ceds-pagination__item").size() mustBe 3
        controls.getElementById("pagination-page_active") must containText("1")
        controls.getElementById("pagination-page_2") must containText("2")
        controls.getElementById("pagination-page_2") must haveHref(controllers.routes.SavedDeclarationsController.displayDeclarations(2))

        controls.getElementById("pagination-page_next") must containText("Next")
        controls.getElementById("pagination-page_next") must haveHref(controllers.routes.SavedDeclarationsController.displayDeclarations(2))
      }

      "there are two pages and the current one is 2" in {
        val controls = paginationComponent(declarations = Seq.fill(10)(declaration), currentPage = 2, elementsTotal = 20)

        controls.getElementsByClass("ceds-pagination__item").size() mustBe 3
        controls.getElementById("pagination-page_previous") must containText("Previous")
        controls.getElementById("pagination-page_previous") must haveHref(controllers.routes.SavedDeclarationsController.displayDeclarations(1))

        controls.getElementById("pagination-page_1") must containText("1")
        controls.getElementById("pagination-page_1") must haveHref(controllers.routes.SavedDeclarationsController.displayDeclarations(1))
        controls.getElementById("pagination-page_active") must containText("2")
      }

      "there are seven pages and the current one is 4" in {
        val controls = paginationComponent(declarations = Seq.fill(10)(declaration), currentPage = 4, elementsTotal = 70)

        controls.getElementsByClass("ceds-pagination__item").size() mustBe 9

        controls.getElementById("pagination-page_previous") must containText("Previous")
        controls.getElementById("pagination-page_previous") must haveHref(controllers.routes.SavedDeclarationsController.displayDeclarations(3))

        controls.getElementById("pagination-page_3") must containText("3")
        controls.getElementById("pagination-page_3") must haveHref(controllers.routes.SavedDeclarationsController.displayDeclarations(3))

        controls.getElementById("pagination-page_active") must containText("4")

        controls.getElementById("pagination-page_5") must containText("5")
        controls.getElementById("pagination-page_5") must haveHref(controllers.routes.SavedDeclarationsController.displayDeclarations(5))

        controls.getElementById("pagination-page_next") must containText("Next")
        controls.getElementById("pagination-page_next") must haveHref(controllers.routes.SavedDeclarationsController.displayDeclarations(5))

        controls.getElementsByClass("ceds-pagination__item ceds-pagination__item--dots").size() mustBe 2
      }
    }
  }
}
