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

package views.summary

import base.Injector
import controllers.general.routes.RootController
import services.cache.ExportsTestHelper
import tools.Stubs
import views.html.summary.summary_page_no_data
import views.common.UnitViewSpec

class SummaryNoDataViewSpec extends UnitViewSpec with Stubs with ExportsTestHelper with Injector {

  val summaryPageNoData = instanceOf[summary_page_no_data]
  val view = summaryPageNoData()(request, messages)

  "Summary page no data" should {

    "display correct page title" in {

      view.getElementById("title").text() mustBe messages("declaration.summary.noData.header", "Standard")
    }

    "display correct hint" in {

      view.getElementsByClass("govuk-body").text() mustBe messages("declaration.summary.noData.header.secondary")
    }

    "display back link which redirect to start page" in {

      val backButton = view.getElementsByClass("govuk-button").first()

      backButton.text() mustBe messages("declaration.summary.noData.button")
      backButton must haveHref(RootController.displayPage)
    }
  }
}
