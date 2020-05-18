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
import forms.declaration.PackageInformation
import models.Mode
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.package_information

class PackageInformationViewSpec extends UnitViewSpec with ExportsTestData with Injector {

  "Package information" should {

    val packageSection = instanceOf[package_information]

    "display title only and change link if Sequence is empty" in {
      val view = packageSection(Mode.Normal, "itemId", 1, Seq.empty)(messages, journeyRequest())
      val row = view.getElementsByClass("package-information-1-row")

      row must haveSummaryKey(messages("declaration.summary.items.item.packageInformation"))
      row must haveSummaryValue("")

      row must haveSummaryActionsText("site.change declaration.summary.items.item.packageInformation.changeAll")

      row must haveSummaryActionsHref(controllers.declaration.routes.PackageInformationSummaryController.displayPage(Mode.Normal, "itemId"))
    }

    "display package information section with multiple package information and change buttons" in {

      val data = Seq(PackageInformation(Some("PB"), Some(123), Some("first-marks")), PackageInformation(Some("QF"), Some(321), Some("second-marks")))

      val view = packageSection(Mode.Normal, "itemId", 1, data)(messages, journeyRequest())
      val table = view.getElementById("package-information-1-table")

      table.getElementsByTag("caption").text() mustBe messages("declaration.summary.items.item.packageInformation")

      table.getElementsByClass("govuk-table__header").get(0).text() mustBe messages("declaration.summary.items.item.packageInformation.type")
      table.getElementsByClass("govuk-table__header").get(1).text() mustBe messages("declaration.summary.items.item.packageInformation.number")
      table.getElementsByClass("govuk-table__header").get(2).text() mustBe messages("declaration.summary.items.item.packageInformation.markings")

      val row1 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(0)
      row1.getElementsByClass("govuk-table__cell").get(0).text() mustBe "Open-ended box and pallet (PB)"
      row1.getElementsByClass("govuk-table__cell").get(1).text() mustBe "123"
      row1.getElementsByClass("govuk-table__cell").get(2).text() mustBe "first-marks"
      val row1ChangeLink = row1.getElementsByClass("govuk-table__cell").get(3).getElementsByTag("a").first()
      row1ChangeLink must haveHref(controllers.declaration.routes.PackageInformationSummaryController.displayPage(Mode.Normal, "itemId"))
      row1ChangeLink.text() mustBe "site.change " + messages("declaration.summary.items.item.packageInformation.change", "PB", "first-marks", 1)

      val row2 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(1)
      row2.getElementsByClass("govuk-table__cell").get(0).text() mustBe "Drum, plastic, non-removable head (QF)"
      row2.getElementsByClass("govuk-table__cell").get(1).text() mustBe "321"
      row2.getElementsByClass("govuk-table__cell").get(2).text() mustBe "second-marks"
      val row2ChangeLink = row2.getElementsByClass("govuk-table__cell").get(3).getElementsByTag("a").first()
      row2ChangeLink must haveHref(controllers.declaration.routes.PackageInformationSummaryController.displayPage(Mode.Normal, "itemId"))
      row2ChangeLink.text() mustBe "site.change " + messages("declaration.summary.items.item.packageInformation.change", "QF", "second-marks", 1)
    }
  }
}
