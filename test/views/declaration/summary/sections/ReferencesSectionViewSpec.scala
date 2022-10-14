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
import controllers.declaration.routes.{AdditionalDeclarationTypeController, ConsignmentReferencesController, LinkDucrToMucrController, MucrController}
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import models.DeclarationStatus.DRAFT
import models.DeclarationType
import models.DeclarationType._
import models.Mode.Draft
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.sections.references_section

import java.time.LocalDateTime

class ReferencesSectionViewSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  val localDateTime = LocalDateTime.of(2019, 11, 28, 14, 48)

  val data = aDeclaration(
    withType(DeclarationType.STANDARD),
    withAdditionalDeclarationType(AdditionalDeclarationType.STANDARD_FRONTIER),
    withConsignmentReferences(ducr = "DUCR", lrn = "LRN"),
    withLinkDucrToMucr(),
    withMucr(),
    withCreatedDate(localDateTime),
    withUpdateDate(localDateTime)
  )

  val section = instanceOf[references_section]

  val view = section(Draft, data)(messages)
  val viewNoAnswers = section(Draft, aDeclaration(withType(DeclarationType.STANDARD)))(messages)

  val expectedCreatedTime = "28 November 2019 at 2:48pm"
  val expectedUpdatedTime = "28 December 2019 at 2:48pm"

  "References section" should {

    "have, inside an Inset Text, a link to /type" when {
      "a declaration has DRAFT status and 'parentDeclarationId' is defined" in {
        val declaration = data.copy(status = DRAFT, parentDeclarationId = Some("some id"))
        val insetText = section(Draft, declaration)(messages).getElementsByClass("govuk-inset-text")
        insetText.size mustBe 1

        val link = insetText.first.getElementsByClass("govuk-link").first
        link.text mustBe messages("declaration.summary.goto.additional.type")
        link must haveHref(AdditionalDeclarationTypeController.displayPage(Draft))
      }
    }

    "not have any Inset Text" when {
      "a declaration has 'parentDeclarationId' undefined" in {
        view.getElementsByClass("govuk-inset-text").size mustBe 0
      }
    }

    "have Date Created" in {
      val row = view.getElementsByClass("createdDate-row")
      row must haveSummaryKey(messages("declaration.summary.references.createdDate"))
      row must haveSummaryValue(expectedCreatedTime)
    }

    "have Expiry Date" in {
      val row = view.getElementsByClass("expiryDate-row")
      row must haveSummaryKey(messages("declaration.summary.references.expireDate"))
      row must haveSummaryValue(expectedUpdatedTime)
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

      row must haveSummaryActionsHref(ConsignmentReferencesController.displayPage(Draft))
    }

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { implicit request =>
      "have LRN with change button" in {
        val view = section(Draft, data.copy(`type` = request.declarationType))(messages)

        val row = view.getElementsByClass("lrn-row")
        row must haveSummaryKey(messages("declaration.summary.references.lrn"))
        row must haveSummaryValue("LRN")

        row must haveSummaryActionsTexts("site.change", "declaration.summary.references.lrn.change")

        row must haveSummaryActionsHref(ConsignmentReferencesController.displayPage(Draft))
      }
    }

    onJourney(SUPPLEMENTARY) { implicit request =>
      "have LRN with change button" in {
        val view = section(Draft, data.copy(`type` = request.declarationType))(messages)

        val row = view.getElementsByClass("lrn-row")
        row must haveSummaryKey(messages("declaration.summary.references.supplementary.lrn"))
        row must haveSummaryValue("LRN")

        row must haveSummaryActionsTexts("site.change", "declaration.summary.references.lrn.change")

        row must haveSummaryActionsHref(ConsignmentReferencesController.displayPage(Draft))
      }
    }

    "have 'Link to a MUCR' with change button" in {
      val row = view.getElementsByClass("linkDucrToMucr-row")
      row must haveSummaryKey(messages("declaration.summary.references.linkDucrToMucr"))
      row must haveSummaryValue(YesNoAnswers.yes)

      row must haveSummaryActionsTexts("site.change", "declaration.summary.references.linkDucrToMucr.change")

      row must haveSummaryActionsHref(LinkDucrToMucrController.displayPage(Draft))
    }

    "have MUCR with change button" in {
      val row = view.getElementsByClass("mucr-row")
      row must haveSummaryKey(messages("declaration.summary.references.mucr"))
      row must haveSummaryValue(MUCR.mucr)

      row must haveSummaryActionsTexts("site.change", "declaration.summary.references.mucr.change")

      row must haveSummaryActionsHref(MucrController.displayPage(Draft))
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
