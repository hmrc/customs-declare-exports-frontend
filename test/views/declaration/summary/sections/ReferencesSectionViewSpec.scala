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
import models.DeclarationStatus.DRAFT
import models.DeclarationType
import models.DeclarationType._
import models.Mode.Change
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.sections.references_section

class ReferencesSectionViewSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  val data = aDeclaration(
    withType(DeclarationType.STANDARD),
    withAdditionalDeclarationType(AdditionalDeclarationType.STANDARD_FRONTIER),
    withConsignmentReferences(ducr = "DUCR", lrn = "LRN"),
    withLinkDucrToMucr(),
    withMucr()
  )

  val section = instanceOf[references_section]

  val view = section(Change, data)(messages)
  val viewNoAnswers = section(Change, aDeclaration(withType(DeclarationType.STANDARD)))(messages)

  "References section" should {

    "have, inside an Inset Text, a link to /type" when {
      "a declaration has DRAFT status and 'parentDeclarationId' is defined" in {
        val declaration = data.copy(status = DRAFT, parentDeclarationId = Some("some id"))
        val insetText = section(Change, declaration)(messages).getElementsByClass("govuk-inset-text")
        insetText.size mustBe 1

        val link = insetText.first.getElementsByClass("govuk-link").first
        link.text mustBe messages("declaration.summary.goto.additional.type")
        link must haveHref(routes.AdditionalDeclarationTypeController.displayPage(Change))
      }
    }

    "not have any Inset Text" when {
      "a declaration has 'parentDeclarationId' undefined" in {
        view.getElementsByClass("govuk-inset-text").size mustBe 0
      }
    }

    "have capitalized declaration type with change button" in {
      val row = view.getElementsByClass("declarationType-row")
      row must haveSummaryKey(messages("declaration.summary.references.type"))
      row must haveSummaryValue("Standard declaration")
    }

    "have additional declaration type with change button" in {
      val row = view.getElementsByClass("additionalType-row")
      row must haveSummaryKey(messages("declaration.summary.references.additionalType"))
      row must haveSummaryValue(messages("declaration.summary.references.additionalType.A"))
    }

    "have DUCR with change button" in {
      val row = view.getElementsByClass("ducr-row")
      row must haveSummaryKey(messages("declaration.summary.references.ducr"))
      row must haveSummaryValue("DUCR")

      row must haveSummaryActionsTexts("site.change", "declaration.summary.references.ducr.change")

      row must haveSummaryActionsHref(routes.ConsignmentReferencesController.displayPage(Change))
    }

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { implicit request =>
      "have LRN with change button" in {
        val view = section(Change, data.copy(`type` = request.declarationType))(messages)

        val row = view.getElementsByClass("lrn-row")
        row must haveSummaryKey(messages("declaration.summary.references.lrn"))
        row must haveSummaryValue("LRN")

        row must haveSummaryActionsTexts("site.change", "declaration.summary.references.lrn.change")

        row must haveSummaryActionsHref(routes.ConsignmentReferencesController.displayPage(Change))
      }
    }

    onJourney(SUPPLEMENTARY) { implicit request =>
      "have LRN with change button" in {
        val view = section(Change, data.copy(`type` = request.declarationType))(messages)

        val row = view.getElementsByClass("lrn-row")
        row must haveSummaryKey(messages("declaration.summary.references.supplementary.lrn"))
        row must haveSummaryValue("LRN")

        row must haveSummaryActionsTexts("site.change", "declaration.summary.references.lrn.change")

        row must haveSummaryActionsHref(routes.ConsignmentReferencesController.displayPage(Change))
      }
    }

    "have 'Link to a MUCR' with change button" in {
      val row = view.getElementsByClass("linkDucrToMucr-row")
      row must haveSummaryKey(messages("declaration.summary.references.linkDucrToMucr"))
      row must haveSummaryValue(YesNoAnswers.yes)

      row must haveSummaryActionsTexts("site.change", "declaration.summary.references.linkDucrToMucr.change")

      row must haveSummaryActionsHref(routes.LinkDucrToMucrController.displayPage(Change))
    }

    "have MUCR with change button" in {
      val row = view.getElementsByClass("mucr-row")
      row must haveSummaryKey(messages("declaration.summary.references.mucr"))
      row must haveSummaryValue(MUCR.mucr)

      row must haveSummaryActionsTexts("site.change", "declaration.summary.references.mucr.change")

      row must haveSummaryActionsHref(routes.MucrController.displayPage(Change))
    }
  }

  "References section with no answers" should {

    "have declaration type" in {
      val row = viewNoAnswers.getElementsByClass("declarationType-row")
      row must haveSummaryKey(messages("declaration.summary.references.type"))
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
