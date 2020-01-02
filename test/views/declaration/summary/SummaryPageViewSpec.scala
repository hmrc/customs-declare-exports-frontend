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

import config.AppConfig
import forms.declaration.LegalDeclaration
import models.Mode
import models.Mode._
import org.jsoup.nodes.Document
import org.mockito.Mockito.when
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.{draft_info_section, summary_page}

import scala.concurrent.duration.FiniteDuration

class SummaryPageViewSpec extends UnitViewSpec with Stubs with ExportsTestData {

  val appConfig = mock[AppConfig]
  when(appConfig.draftTimeToLive).thenReturn(FiniteDuration(30, "day"))
  val draftInfoPage = new draft_info_section(appConfig)

  val summaryPage = new summary_page(mainTemplate, draftInfoPage)
  def view(mode: Mode = Normal): Document = summaryPage(mode, LegalDeclaration.form())(journeyRequest(aDeclaration()), messages, minimalAppConfig)

  "Summary page" should {

    "should display correct title" when {

      "mode is normal" in {

        view().getElementById("title").text() must include("declaration.summary.normal-header")
      }

      "mode is amend" in {

        view(Amend).getElementById("title").text() must include("declaration.summary.amend-header")
      }

      "mode is draft" in {

        view(Draft).getElementById("title").text() must include("declaration.summary.saved-header")
      }
    }

    "should display correct back link" when {

      "mode is normal" in {

        val backButton = view().getElementById("back-link")

        backButton.text() mustBe messages("site.back")
        backButton must haveHref(controllers.declaration.routes.TransportContainerController.displayContainerSummary(Normal))
      }

      "mode is amend" in {

        val backButton = view(Amend).getElementById("back-link")

        backButton.text() mustBe messages("supplementary.summary.back")
        backButton must haveHref(controllers.routes.SubmissionsController.displayListOfSubmissions())
      }

      "mode is draft" in {

        val backButton = view(Draft).getElementById("back-link")

        backButton.text() mustBe messages("site.back")
        backButton must haveHref(controllers.routes.SavedDeclarationsController.displayDeclarations())
      }
    }

    "have references section" in {

      view().getElementById("declaration-references-summary").text() mustNot be(empty)
    }

    "have parties section" in {

      view().getElementById("declaration-parties-summary").text() mustNot be(empty)
    }

    "have countries section" in {

      view().getElementById("declaration-countries-summary").text() mustNot be(empty)
    }

    "have locations section" in {

      view().getElementById("declaration-locations-summary").text() mustNot be(empty)
    }

    "have transaction section" in {

      view().getElementById("declaration-transaction-summary").text() mustNot be(empty)
    }

    "have items section" in {

      view().getElementById("declaration-items-summary").text() mustNot be(empty)
    }

    "have warehouse section" in {

      view().getElementById("declaration-warehouse-summary").text() mustNot be(empty)
    }

    "have transport section" in {

      view().getElementById("declaration-transport-summary").text() mustNot be(empty)
    }
  }
}
