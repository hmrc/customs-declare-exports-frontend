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

import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import models.DeclarationType
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.references_section

class ReferencesSectionViewSpec extends UnitViewSpec with ExportsTestData {

  val data = aDeclaration(
    withType(DeclarationType.STANDARD),
    withDispatchLocation(),
    withAdditionalDeclarationType(AdditionalDeclarationType.STANDARD_FRONTIER),
    withConsignmentReferences(ducr = "DUCR", lrn = "LRN")
  )

  val view = references_section(data)(messages, journeyRequest())

  "References section" should {

    "have capitalized declaration type" in {

      view.getElementById("declarationType-label").text() mustBe messages("declaration.summary.references.type")
      view.getElementById("declarationType").text() mustBe "Standard"
    }

    "have dispatch location" in {

      view.getElementById("location-label").text() mustBe messages("declaration.summary.references.location")
      view.getElementById("location").text() mustBe messages("site.yes")
    }

    "have additional declaration type" in {

      view.getElementById("additionalType-label").text() mustBe messages("declaration.summary.references.additionalType")
      view.getElementById("additionalType").text() mustBe messages("declaration.summary.references.additionalType.A")
    }

    "have ducr" in {

      view.getElementById("ducr-label").text() mustBe messages("declaration.summary.references.ducr")
      view.getElementById("ducr").text() mustBe "DUCR"
    }

    "have lrn" in {

      view.getElementById("lrn-label").text() mustBe messages("declaration.summary.references.lrn")
      view.getElementById("lrn").text() mustBe "LRN"
    }
  }
}
