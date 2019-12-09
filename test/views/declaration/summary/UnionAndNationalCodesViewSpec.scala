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

import forms.declaration.AdditionalInformation
import models.Mode
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.union_and_national_codes

class UnionAndNationalCodesViewSpec extends UnitViewSpec with ExportsTestData {

  "Union and national codes" should {

    "be empty" when {

      "there is no additional information" in {

        union_and_national_codes("itemId", 1, Seq.empty)(messages, journeyRequest()).text() mustBe empty
      }
    }

    "display additional information with change buttons" in {

      val data = Seq(AdditionalInformation("12345", "description1"), AdditionalInformation("23456", "description2"))
      val view = union_and_national_codes("itemId", 1, data)(messages, journeyRequest())

      view.getElementById("additional-information-1").text() mustBe messages("declaration.summary.items.item.additionalInformation")
      view.getElementById("additional-information-code-1").text() mustBe messages("declaration.summary.items.item.additionalInformation.code")
      view.getElementById("additional-information-information-1").text() mustBe messages(
        "declaration.summary.items.item.additionalInformation.information"
      )
      view.getElementById("additional-information-1-code-0").text() mustBe "12345"
      view.getElementById("additional-information-1-information-0").text() mustBe "description1"
      view.getElementById("additional-information-1-change-0").text() mustBe messages("site.change")
      view.getElementById("additional-information-1-change-0") must haveHref(
        controllers.declaration.routes.AdditionalInformationController.displayPage(Mode.Normal, "itemId")
      )
      view.getElementById("additional-information-1-code-1").text() mustBe "23456"
      view.getElementById("additional-information-1-information-1").text() mustBe "description2"
      view.getElementById("additional-information-1-change-1").text() mustBe messages("site.change")
      view.getElementById("additional-information-1-change-1") must haveHref(
        controllers.declaration.routes.AdditionalInformationController.displayPage(Mode.Normal, "itemId")
      )
    }
  }
}
