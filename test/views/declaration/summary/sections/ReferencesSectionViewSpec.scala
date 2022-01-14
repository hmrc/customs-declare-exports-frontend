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

package views.declaration.summary.sections

import base.Injector
import controllers.declaration.routes
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.{DeclarationType, Mode}
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.sections.references_section

class ReferencesSectionViewSpec extends UnitViewSpec with ExportsTestData with Injector {

  val data = aDeclaration(
    withType(DeclarationType.STANDARD),
    withAdditionalDeclarationType(AdditionalDeclarationType.STANDARD_FRONTIER),
    withConsignmentReferences(ducr = "DUCR", lrn = "LRN"),
    withLinkDucrToMucr(),
    withMucr()
  )

  val section = instanceOf[references_section]

  val view = section(Mode.Change, data)(messages)
  val viewNoAnswers = section(Mode.Change, aDeclaration(withType(DeclarationType.STANDARD)))(messages)

  "References section" should {

    "have capitalized declaration type with change button" in {

      val row = view.getElementsByClass("declarationType-row")
      row must haveSummaryKey(messages("declaration.summary.references.type"))
      row must haveSummaryValue("Standard declaration")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.references.type.change")

      row must haveSummaryActionsHref(routes.DeclarationChoiceController.displayPage(Mode.Change))
    }

    "have additional declaration type with change button" in {

      val row = view.getElementsByClass("additionalType-row")
      row must haveSummaryKey(messages("declaration.summary.references.additionalType"))
      row must haveSummaryValue(messages("declaration.summary.references.additionalType.A"))

      row must haveSummaryActionsTexts("site.change", "declaration.summary.references.additionalType.change")

      row must haveSummaryActionsHref(routes.AdditionalDeclarationTypeController.displayPage(Mode.Change))
    }

    "have DUCR with change button" in {

      val row = view.getElementsByClass("ducr-row")
      row must haveSummaryKey(messages("declaration.summary.references.ducr"))
      row must haveSummaryValue("DUCR")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.references.ducr.change")

      row must haveSummaryActionsHref(routes.ConsignmentReferencesController.displayPage(Mode.Change))
    }

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { implicit request =>
      "have LRN with change button" in {

        val view = section(Mode.Change, data.copy(`type` = request.declarationType))(messages)

        val row = view.getElementsByClass("lrn-row")
        row must haveSummaryKey(messages("declaration.summary.references.lrn"))
        row must haveSummaryValue("LRN")

        row must haveSummaryActionsTexts("site.change", "declaration.summary.references.lrn.change")

        row must haveSummaryActionsHref(routes.ConsignmentReferencesController.displayPage(Mode.Change))
      }
    }

    onJourney(SUPPLEMENTARY) { implicit request =>
      "have LRN with change button" in {

        val view = section(Mode.Change, data.copy(`type` = request.declarationType))(messages)

        val row = view.getElementsByClass("lrn-row")
        row must haveSummaryKey(messages("declaration.summary.references.supplementary.lrn"))
        row must haveSummaryValue("LRN")

        row must haveSummaryActionsTexts("site.change", "declaration.summary.references.lrn.change")

        row must haveSummaryActionsHref(routes.ConsignmentReferencesController.displayPage(Mode.Change))
      }
    }

    "have 'Link to a MUCR' with change button" in {

      val row = view.getElementsByClass("linkDucrToMucr-row")
      row must haveSummaryKey(messages("declaration.summary.references.linkDucrToMucr"))
      row must haveSummaryValue(YesNoAnswers.yes)

      row must haveSummaryActionsTexts("site.change", "declaration.summary.references.linkDucrToMucr.change")

      row must haveSummaryActionsHref(routes.LinkDucrToMucrController.displayPage(Mode.Change))
    }

    "have MUCR with change button" in {

      val row = view.getElementsByClass("mucr-row")
      row must haveSummaryKey(messages("declaration.summary.references.mucr"))
      row must haveSummaryValue(MUCR.mucr)

      row must haveSummaryActionsTexts("site.change", "declaration.summary.references.mucr.change")

      row must haveSummaryActionsHref(routes.MucrController.displayPage(Mode.Change))
    }
  }

  "References section with no answers" should {

    "have declaration type" in {

      val row = viewNoAnswers.getElementsByClass("declarationType-row")
      row must haveSummaryKey(messages("declaration.summary.references.type"))
      row must haveSummaryActionsHref(routes.DeclarationChoiceController.displayPage(Mode.Change))
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
