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

package views.declaration.summary.sections

import base.Injector
import controllers.declaration.routes.ItemsSummaryController
import forms.common.YesNoAnswer
import forms.declaration.Document
import forms.declaration.NatureOfTransaction.BusinessPurchase
import models.DeclarationType.{CLEARANCE, STANDARD}
import models.ExportsDeclaration
import models.declaration.Transport
import org.scalatest.compatible.Assertion
import play.twirl.api.HtmlFormat.Appendable
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.sections.items_section

class ItemsSectionViewSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  private val itemsSection = instanceOf[items_section]

  def createView(declaration: ExportsDeclaration, actionsEnabled: Boolean = true): Appendable =
    itemsSection(declaration, actionsEnabled)(request, messages)

  "Items section" when {

    "the declaration has items" should {
      "display the 'Items' section and" should {
        val item1 = anItem(withProcedureCodes(Some("code")))

        "display the 'Add item' link" when {
          "the declaration type is NOT CLEARANCE" in {
            val item2 = anItem(withStatisticalValue("10"))
            val declaration = aDeclaration(withType(STANDARD), withItems(item1, item2))
            val view = createView(declaration)
            checkSectionHeader(view, true)

            val items = view.getElementsByTag("h3")
            items.size() mustBe 2
            for (ix <- 0 to 1)
              items.get(ix).text() mustBe messages("declaration.summary.items.item.presentationId", ix + 1)
          }
        }

        "NOT display the 'Add item' link" when {
          "the declaration type is CLEARANCE" in {
            val declaration = aDeclaration(withType(CLEARANCE), withItems(item1))
            val view = createView(declaration)
            view.getElementsByTag("form").size() mustBe 0
            view.getElementsByClass("input-submit").size() mustBe 0

            val items = view.getElementsByTag("h3")
            items.size() mustBe 1
            items.get(0).text() mustBe messages("declaration.summary.items.item.presentationId", 1)
          }
        }
      }
    }

    "the declaration has no items" should {

      "NOT display the 'Items' section" when {

        "actionEnabled is false" in {
          val view = createView(aDeclaration(), false)
          view.toString().replace("\n", "") mustBe ""
        }

        "actionEnabled is true and" when {
          "declaration.previousDocuments is undefined and" when {
            "the transport section is empty" in {
              val view = createView(aDeclaration(withNatureOfTransaction(BusinessPurchase)))
              view.toString().replace("\n", "") mustBe ""
            }
          }
        }
      }

      "only display the header row of the 'Items' section" when {
        "actionEnabled is true and" when {

          "declaration.previousDocuments is defined" in {
            val view = createView(aDeclaration(withPreviousDocuments(Document("355", "reference", None))))
            checkSectionHeader(view)

            val items = view.getElementsByTag("h3")
            items.size() mustBe 0
          }

          "the transport section is NOT empty" in {
            val declaration = aDeclaration().copy(transport = Transport(expressConsignment = YesNoAnswer.Yes))
            val view = createView(declaration)
            checkSectionHeader(view)

            val items = view.getElementsByTag("h3")
            items.size() mustBe 0
          }
        }
      }
    }

    "the declaration has only empty items" should {

      "NOT display the 'Items' section" when {

        "actionEnabled is false" in {
          val view = createView(aDeclaration(withItems(2)), false)
          view.toString().replace("\n", "") mustBe ""
        }

        "actionEnabled is true and" when {
          "declaration.previousDocuments is undefined and" when {
            "the transport section is empty" in {
              val view = createView(aDeclaration(withItems(2), withNatureOfTransaction(BusinessPurchase)))
              view.toString().replace("\n", "") mustBe ""
            }
          }
        }
      }

      "only display the header row of the 'Items' section" when {
        "actionEnabled is true and" when {

          "declaration.previousDocuments is defined" in {
            val view = createView(aDeclaration(withItems(2), withPreviousDocuments(Document("355", "reference", None))))
            checkSectionHeader(view)

            val items = view.getElementsByTag("h3")
            items.size() mustBe 0
          }

          "the transport section is NOT empty" in {
            val declaration = aDeclaration(withItems(2)).copy(transport = Transport(expressConsignment = YesNoAnswer.Yes))
            val view = createView(declaration)
            checkSectionHeader(view)

            val items = view.getElementsByTag("h3")
            items.size() mustBe 0
          }
        }
      }
    }
  }

  def checkSectionHeader(view: Appendable, withItems: Boolean = false): Assertion = {
    view.getElementsByTag("h2").text mustBe messages("declaration.summary.items")

    if (withItems) {
      val form = view.getElementsByTag("form").get(0)
      form.attr("action").text() mustBe ItemsSummaryController.addFirstItem.url

      val input = form.getElementsByClass("input-submit").get(0)
      input.attr("value") mustBe messages("declaration.summary.items.add")
    } else {
      val link = view.getElementsByTag("a").get(0)
      link.attr("href") mustBe ItemsSummaryController.displayAddItemPage.url
      link.text mustBe messages("declaration.summary.items.add")
    }
  }
}
