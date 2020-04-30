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
import config.AppConfig
import forms.common.YesNoAnswer.YesNoAnswers
import forms.common.YesNoAnswer.YesNoAnswers.yes
import forms.declaration.{CommodityDetails, LegalDeclaration, WarehouseIdentification}
import models.ExportsDeclaration
import models.Mode._
import org.jsoup.nodes.Document
import org.mockito.Mockito.when
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary._

import scala.concurrent.duration.FiniteDuration

class SummaryPageViewSpec extends UnitViewSpec with Stubs with ExportsTestData with Injector {

  val appConfig = mock[AppConfig]
  private val realMessages = validatedMessages
  when(appConfig.draftTimeToLive).thenReturn(FiniteDuration(30, "day"))
  val draftInfoPage = instanceOf[draft_info_section]

  val normal_summaryPage = instanceOf[normal_summary_page]
  def view(declaration: ExportsDeclaration = aDeclaration()): Document =
    normal_summaryPage(LegalDeclaration.form())(journeyRequest(declaration), realMessages, minimalAppConfig)

  "Summary page" should {

    val document = view()

    "should display correct title" in {

      document.getElementById("title").text() mustBe realMessages("declaration.summary.normal-header")
    }

    "should display correct back link" in {

      val backButton = document.getElementById("back-link")

      backButton.text() mustBe realMessages("site.back")
      backButton must haveHref(controllers.declaration.routes.TransportContainerController.displayContainerSummary(Normal))
    }

    "have references section" in {

      document.getElementById("declaration-references-summary").text() mustNot be(empty)
    }

    "not have parties section" in {

      view().getElementById("declaration-parties-summary") mustBe null
    }

    "have parties section" in {

      view(declaration = aDeclaration(withExporterDetails())).getElementById("declaration-parties-summary").text() mustNot be(empty)
    }

    "not have countries section" in {

      view().getElementById("declaration-countries-summary") mustBe null
    }

    "have countries section" in {

      view(declaration = aDeclaration(withDestinationCountry())).getElementById("declaration-countries-summary").text() mustNot be(empty)
    }

    "not have locations section" in {

      view().getElementById("declaration-locations-summary") mustBe null
    }

    "have locations section with UK office of exit" in {

      view(declaration = aDeclaration(withOfficeOfExit(officeId = "office-Id", answer = yes)))
        .getElementById("declaration-locations-summary")
        .text() must include("office-Id")
    }

    "have locations section with office of exit outside UK" in {

      view(declaration = aDeclaration(withOfficeOfExit(), withOfficeOfExitOutsideUk("office-id-outside-uk")))
        .getElementById("declaration-locations-summary")
        .text() must include("office-id-outside-uk")
    }

    "not have transaction section" in {

      view().getElementById("declaration-transaction-summary") mustBe null
    }

    "have transaction section" in {

      view(declaration = aDeclaration(withNatureOfTransaction("1"))).getElementById("declaration-transaction-summary").text() mustNot be(empty)
    }

    "not have items section" in {

      view().getElementById("declaration-items-summary") mustBe null
    }

    "have items section" in {

      val details = CommodityDetails(Some("12345678"), Some("Description"))
      view(declaration = aDeclaration(withItem(anItem(withCommodityDetails(details)))))
        .getElementById("declaration-items-summary-0")
        .text() mustNot be(empty)
    }

    "not have warehouse section" in {

      view().getElementById("declaration-warehouse-summary") mustBe null
    }

    "have warehouse section" in {

      view(declaration = aDeclaration(withWarehouseIdentification(Some(WarehouseIdentification(Some("12345"))))))
        .getElementById("declaration-warehouse-summary")
        .text() mustNot be(empty)
    }

    "not have transport section" in {

      view().getElementById("declaration-transport-summary") mustBe null
    }

    "have transport section" in {

      view(declaration = aDeclaration(withBorderTransport())).getElementById("declaration-transport-summary").text() mustNot be(empty)
    }
  }
}
