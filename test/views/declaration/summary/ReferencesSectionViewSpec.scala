/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import models.{DeclarationType, Mode}
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.references_section

class ReferencesSectionViewSpec extends UnitViewSpec with ExportsTestData with Injector {

  val data = aDeclaration(
    withType(DeclarationType.STANDARD),
    withDispatchLocation(),
    withAdditionalDeclarationType(AdditionalDeclarationType.STANDARD_FRONTIER),
    withConsignmentReferences(ducr = "DUCR", lrn = "LRN")
  )

  val section = instanceOf[references_section]

  val view = section(Mode.Change, data)(messages, journeyRequest())
  val viewNoAnswers = section(Mode.Change, aDeclaration(withType(DeclarationType.STANDARD)))(messages, journeyRequest())

  "References section" should {

    "have capitalized declaration type with change button" in {

      val row = view.getElementsByClass("declarationType-row")
      row must haveSummaryKey(messages("declaration.summary.references.type"))
      row must haveSummaryValue("Standard declaration")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.references.type.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.DeclarationChoiceController.displayPage(Mode.Change))
    }

    "have additional declaration type with change button" in {

      val row = view.getElementsByClass("additionalType-row")
      row must haveSummaryKey(messages("declaration.summary.references.additionalType"))
      row must haveSummaryValue(messages("declaration.summary.references.additionalType.A"))

      row must haveSummaryActionsTexts("site.change", "declaration.summary.references.additionalType.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.AdditionalDeclarationTypeController.displayPage(Mode.Change))
    }

    "have ducr with change button" in {

      val row = view.getElementsByClass("ducr-row")
      row must haveSummaryKey(messages("declaration.summary.references.ducr"))
      row must haveSummaryValue("DUCR")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.references.ducr.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.ConsignmentReferencesController.displayPage(Mode.Change))
    }

    "have lrn with change button" in {

      val row = view.getElementsByClass("lrn-row")
      row must haveSummaryKey(messages("declaration.summary.references.lrn"))
      row must haveSummaryValue("LRN")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.references.lrn.change")

      row must haveSummaryActionsHref(controllers.declaration.routes.ConsignmentReferencesController.displayPage(Mode.Change))
    }
  }

  "References section with no answers" should {

    "have declaration type" in {

      val row = viewNoAnswers.getElementsByClass("declarationType-row")
      row must haveSummaryKey(messages("declaration.summary.references.type"))
      row must haveSummaryActionsHref(controllers.declaration.routes.DeclarationChoiceController.displayPage(Mode.Change))
    }

    "not have dispatch location" in {

      viewNoAnswers.getElementsByClass("location-row") mustBe empty
    }

    "not have additional declaration type" in {

      viewNoAnswers.getElementsByClass("additionalType-row") mustBe empty
    }

    "not have have ducr" in {

      viewNoAnswers.getElementsByClass("ducr-row") mustBe empty
    }

    "not have have lrn" in {

      viewNoAnswers.getElementsByClass("lrn-row") mustBe empty
    }
  }
}
