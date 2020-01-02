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

import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.items_section

class ItemsSectionViewSpec extends UnitViewSpec with ExportsTestData {

  "Items section" should {

    "display items empty" when {

      "there is no items in the declaration" in {

        val view = items_section(aDeclaration())(messages, journeyRequest())

        view.getElementById("empty-items").text() mustBe messages("declaration.summary.items.empty")
      }

      "display items if exists" in {

        val data = aDeclaration(withItems(anItem(withSequenceId(1)), anItem(withSequenceId(2))))

        val view = items_section(data)(messages, journeyRequest())

        view.getElementById("item-1-header").text() mustNot be(empty)
        view.getElementById("item-2-header").text() mustNot be(empty)
      }
    }
  }
}
