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

package views.declaration.summary

import base.Injector
import forms.declaration.CommodityDetails
import models.ExportsDeclaration
import org.jsoup.nodes.Document
import tools.Stubs
import views.declaration.spec.UnitViewSpec

trait SummaryPageViewSpec extends UnitViewSpec with Injector with Stubs {

  def commonBehaviour(document: Document): Unit =
    "have references section" in {
      document.getElementById("declaration-references-summary").text() mustNot be(empty)
    }

  def displayWarning(document: Document): Unit =
    "warning text should be displayed" in {
      val warningText = s"! ${messages("site.warning")} ${messages("declaration.summary.warning")}"
      document.getElementsByClass("govuk-warning-text").text mustBe warningText
    }

  // scalastyle:off
  def sectionsVisiblity(view: ExportsDeclaration => Document): Unit = {
    "not have parties section" in {
      view(aDeclaration()).getElementById("declaration-parties-summary") mustBe null
    }

    "have parties section" in {
      view(aDeclaration(withExporterDetails())).getElementById("declaration-parties-summary").text() mustNot be(empty)
    }

    "not have countries section" in {
      view(aDeclaration()).getElementById("declaration-countries-summary") mustBe null
    }

    "have countries section" in {
      view(aDeclaration(withDestinationCountry())).getElementById("declaration-countries-summary").text() mustNot be(empty)
    }

    "not have locations section" in {
      view(aDeclaration()).getElementById("declaration-locations-summary") mustBe null
    }

    "have locations section with UK office of exit" in {
      view(aDeclaration(withOfficeOfExit(officeId = "office-Id")))
        .getElementById("declaration-locations-summary")
        .text() must include("office-Id")
    }

    "not have transaction section" in {
      view(aDeclaration()).getElementById("declaration-transaction-summary") mustBe null
    }

    "have transaction section" in {
      view(aDeclaration(withNatureOfTransaction("1"))).getElementById("declaration-transaction-summary").text() mustNot be(empty)
    }

    "not have items section" in {
      view(aDeclaration()).getElementById("declaration-items-summary") mustBe null
    }

    "have items section" in {
      val details = CommodityDetails(Some("1234567890"), Some("Description"))
      view(aDeclaration(withItem(anItem(withCommodityDetails(details)))))
        .getElementById("declaration-items-summary-0")
        .text() mustNot be(empty)
    }

    "not have transport section" in {
      view(aDeclaration()).getElementById("declaration-transport-summary") mustBe null
    }

    "have transport section" in {
      view(aDeclaration(withBorderTransport())).getElementById("declaration-transport-summary").text() mustNot be(empty)
    }
  }
  // scalastyle:on
}
