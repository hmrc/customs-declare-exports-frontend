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

import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import models.{DeclarationType, Mode}
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

  val view = references_section(Mode.Change, data)(messages, journeyRequest())
  val viewNoAnswers = references_section(Mode.Change, aDeclaration(withType(DeclarationType.STANDARD)))(messages, journeyRequest())

  "References section" should {

    "have capitalized declaration type with change button" in {

      view.getElementById("declarationType-label").text() mustBe messages("declaration.summary.references.type")
      view.getElementById("declarationType").text() mustBe "Standard"

      val List(change, accessibleChange) = view.getElementById("declarationType-change").text().split(" ").toList

      change mustBe messages("site.change")
      accessibleChange mustBe messages("declaration.summary.references.type.change")

      view.getElementById("declarationType-change") must haveHref(controllers.declaration.routes.DeclarationChoiceController.displayPage(Mode.Change))
    }

    "have dispatch location with change button" in {

      view.getElementById("location-label").text() mustBe messages("declaration.summary.references.location")
      view.getElementById("location").text() mustBe messages("site.yes")

      val List(change, accessibleChange) = view.getElementById("location-change").text().split(" ").toList

      change mustBe messages("site.change")
      accessibleChange mustBe messages("declaration.summary.references.location.change")

      view.getElementById("location-change") must haveHref(controllers.declaration.routes.DispatchLocationController.displayPage(Mode.Change))
    }

    "have additional declaration type with change button" in {

      view.getElementById("additionalType-label").text() mustBe messages("declaration.summary.references.additionalType")
      view.getElementById("additionalType").text() mustBe messages("declaration.summary.references.additionalType.A")

      val List(change, accessibleChange) = view.getElementById("additionalType-change").text().split(" ").toList

      change mustBe messages("site.change")
      accessibleChange mustBe messages("declaration.summary.references.additionalType.change")

      view.getElementById("additionalType-change") must haveHref(controllers.declaration.routes.AdditionalDeclarationTypeController.displayPage(Mode.Change))
    }

    "have ducr with change button" in {

      view.getElementById("ducr-label").text() mustBe messages("declaration.summary.references.ducr")
      view.getElementById("ducr").text() mustBe "DUCR"

      val List(change, accessibleChange) = view.getElementById("ducr-change").text().split(" ").toList

      change mustBe messages("site.change")
      accessibleChange mustBe messages("declaration.summary.references.ducr.change")

      view.getElementById("ducr-change") must haveHref(controllers.declaration.routes.ConsignmentReferencesController.displayPage(Mode.Change))
    }

    "have lrn with change button" in {

      view.getElementById("lrn-label").text() mustBe messages("declaration.summary.references.lrn")
      view.getElementById("lrn").text() mustBe "LRN"

      val List(change, accessibleChange) = view.getElementById("lrn-change").text().split(" ").toList

      change mustBe messages("site.change")
      accessibleChange mustBe messages("declaration.summary.references.lrn.change")

      view.getElementById("lrn-change") must haveHref(controllers.declaration.routes.ConsignmentReferencesController.displayPage(Mode.Change))
    }
  }

  "References section with no answers" should {

    "have declaration type" in {

      viewNoAnswers.getElementById("declarationType-label").text() mustBe messages("declaration.summary.references.type")
      viewNoAnswers.getElementById("declarationType-change") must haveHref(controllers.declaration.routes.DeclarationChoiceController.displayPage(Mode.Change))
    }

    "not have dispatch location" in {

      viewNoAnswers.getElementById("location-label") mustBe null
    }

    "not have additional declaration type" in {

      viewNoAnswers.getElementById("additionalType-label") mustBe null
    }

    "not have have ducr" in {

      viewNoAnswers.getElementById("ducr-label") mustBe null
    }

    "not have have lrn" in {

      viewNoAnswers.getElementById("lrn-label") mustBe null
    }
  }
}
