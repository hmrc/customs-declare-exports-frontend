/*
 * Copyright 2019 HM Revenue & Customs
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

import forms.declaration.PackageInformation
import models.Mode
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.package_information

class PackageInformationViewSpec extends UnitViewSpec with ExportsTestData {

  "Package information" should {

    "be empty if Sequence is empty" in {

      package_information("itemId", 1, Seq.empty)(messages, journeyRequest()).text() mustBe empty
    }

    "display package information section with multiple package information and change buttons" in {

      val data = Seq(PackageInformation("PB", 123, "first-marks"), PackageInformation("QF", 321, "second-marks"))

      val view = package_information("itemId", 1, data)(messages, journeyRequest())

      view.getElementById("package-information-1").text() mustBe messages("declaration.summary.items.item.packageInformation")
      view.getElementById("package-information-type-1").text() mustBe messages("declaration.summary.items.item.packageInformation.type")
      view.getElementById("package-information-number-1").text() mustBe messages("declaration.summary.items.item.packageInformation.number")
      view.getElementById("package-information-markings-1").text() mustBe messages("declaration.summary.items.item.packageInformation.markings")
      view.getElementById("package-information-1-code-0").text() mustBe "Open-ended box and pallet - PB"
      view.getElementById("package-information-1-number-0").text() mustBe "123"
      view.getElementById("package-information-1-marks-0").text() mustBe "first-marks"
      view.getElementById("package-information-1-change-0").text() mustBe messages("site.change")
      view.getElementById("package-information-1-change-0") must haveHref(
        controllers.declaration.routes.PackageInformationController.displayPage(Mode.Normal, "itemId")
      )
      view.getElementById("package-information-1-code-1").text() mustBe "Drum, plastic, non-removable head - QF"
      view.getElementById("package-information-1-number-1").text() mustBe "321"
      view.getElementById("package-information-1-marks-1").text() mustBe "second-marks"
      view.getElementById("package-information-1-change-1").text() mustBe messages("site.change")
      view.getElementById("package-information-1-change-1") must haveHref(
        controllers.declaration.routes.PackageInformationController.displayPage(Mode.Normal, "itemId")
      )
    }
  }
}
