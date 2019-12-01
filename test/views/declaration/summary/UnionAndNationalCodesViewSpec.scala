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
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.union_and_national_codes

class UnionAndNationalCodesViewSpec extends UnitViewSpec with ExportsTestData {

  "Union and national codes" should {

    "be empty" when {

      "there is no additional information" in {

        union_and_national_codes(Seq.empty)(messages, journeyRequest()).text() mustBe empty
      }
    }

    "display additional information" in {

      val data = Seq(AdditionalInformation("12345", "description1"), AdditionalInformation("23456", "description2"))
      val view = union_and_national_codes(data)(messages, journeyRequest())

      view.getElementById("additional-information").text() mustBe messages("declaration.summary.items.item.additionalInformation")
      view.getElementById("additional-information-code").text() mustBe messages("declaration.summary.items.item.additionalInformation.code")
      view.getElementById("additional-information-information").text() mustBe messages(
        "declaration.summary.items.item.additionalInformation.information"
      )
      view.getElementById("additional-information-0-code").text() mustBe "12345"
      view.getElementById("additional-information-0-information").text() mustBe "description1"
      view.getElementById("additional-information-1-code").text() mustBe "23456"
      view.getElementById("additional-information-1-information").text() mustBe "description2"
    }
  }
}
