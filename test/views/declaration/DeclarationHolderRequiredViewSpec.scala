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

package views.declaration

import base.Injector
import controllers.declaration.routes
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.STANDARD_PRE_LODGED
import forms.declaration.AuthorisationProcedureCodeChoice
import models.requests.JourneyRequest
import models.DeclarationType._
import models.Mode
import models.declaration.AuthorisationProcedureCode._
import models.declaration.Parties
import org.jsoup.nodes.Document
import services.cache.ExportsTestData
import tools.Stubs
import views.components.gds.Styles
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.declarationHolder.declaration_holder_required
import views.tags.ViewTest

@ViewTest
class DeclarationHolderRequiredViewSpec extends UnitViewSpec with ExportsTestData with CommonMessages with Stubs with Injector {
  private val form = YesNoAnswer.form()
  private val declarationHolderRequiredPage = instanceOf[declaration_holder_required]
  private def view(implicit request: JourneyRequest[_]): Document =
    declarationHolderRequiredPage(Mode.Normal, form)

  "Declaration Holder Required View" should {

    "have correct message keys" in {
      messages must haveTranslationFor("declaration.declarationHolderRequired.title")
      messages must haveTranslationFor("declaration.declarationHolderRequired.mainText.default")
      messages must haveTranslationFor("declaration.declarationHolderRequired.mainText.standard_prelodged_1040")
      messages must haveTranslationFor("declaration.declarationHolderRequired.mainText.occasional.1")
      messages must haveTranslationFor("declaration.declarationHolderRequired.mainText.occasional.2")
      messages must haveTranslationFor("declaration.declarationHolderRequired.mainText.standard_prelodged_other")
      messages must haveTranslationFor("declaration.declarationHolderRequired.inset.para1")
      messages must haveTranslationFor("declaration.declarationHolderRequired.inset.para2")
      messages must haveTranslationFor("declaration.declarationHolderRequired.inset.bullet1.text")
      messages must haveTranslationFor("declaration.declarationHolderRequired.inset.bullet2.text")
      messages must haveTranslationFor("declaration.declarationHolderRequired.tradeTariff.link")
      messages must haveTranslationFor("tariff.declaration.addAuthorisationRequired.clearance.text")
      messages must haveTranslationFor("declaration.declarationHolderRequired.empty")
    }
  }

  "Declaration Holder Required View on empty page" should {
    onEveryDeclarationJourney() { implicit request =>
      "display page title" in {
        view.getElementsByClass(Styles.gdsPageLegend) must containMessageForElements("declaration.declarationHolderRequired.title")
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.2")
      }

      "display radio button with Yes option" in {
        view.getElementById("code_yes").attr("value") mustBe YesNoAnswers.yes
        view.getElementsByAttributeValue("for", "code_yes") must containMessageForElements("site.yes")
      }

      "display radio button with No option" in {
        view.getElementById("code_no").attr("value") mustBe YesNoAnswers.no
        view.getElementsByAttributeValue("for", "code_no") must containMessageForElements("site.no")
      }

      "display 'Save and continue' button on page" in {
        val saveButton = view.getElementById("submit")
        saveButton must containMessage(saveAndContinueCaption)
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, CLEARANCE) { implicit request =>
      "display inset text" in {
        val inset = view.getElementsByClass("govuk-inset-text")
        val expected = Seq(
          messages("declaration.declarationHolderRequired.inset.para1"),
          messages("declaration.declarationHolderRequired.inset.bullet1.text"),
          messages("declaration.declarationHolderRequired.inset.bullet2.text"),
          messages("declaration.declarationHolderRequired.inset.para2")
        ).mkString(" ")
        inset.get(0) must containText(expected)
      }
    }

    onJourney(OCCASIONAL) { implicit request =>
      "not display inset text" in {
        view.getElementsByClass("govuk-inset-text").size() mustBe 0
      }
    }

    "display main text" that {
      onJourney(SUPPLEMENTARY, SIMPLIFIED, CLEARANCE) { implicit request =>
        "content is correct for that journey " in {
          view.getElementsByClass("mainText").get(0) must containText(messages("declaration.declarationHolderRequired.mainText.default"))
        }
      }

      onStandard { implicit request =>
        "'1040' selected then content is correct for that journey " in {
          val parties = Parties(authorisationProcedureCodeChoice = Some(AuthorisationProcedureCodeChoice(Code1040)))
          val req = journeyRequest(request.cacheModel.copy(additionalDeclarationType = Some(STANDARD_PRE_LODGED), parties = parties))

          view(req).getElementsByClass("mainText").get(0) must containText(
            messages("declaration.declarationHolderRequired.mainText.standard_prelodged_1040")
          )
        }

        "'1007' selected then content is correct for that journey " in {
          val parties = Parties(authorisationProcedureCodeChoice = Some(AuthorisationProcedureCodeChoice(Code1007)))
          val req = journeyRequest(request.cacheModel.copy(additionalDeclarationType = Some(STANDARD_PRE_LODGED), parties = parties))

          view(req).getElementsByClass("mainText").get(0) must containText(messages("declaration.declarationHolderRequired.mainText.default"))
        }

        "'other' selected then content is correct for that journey " in {
          val parties = Parties(authorisationProcedureCodeChoice = Some(AuthorisationProcedureCodeChoice(CodeOther)))
          val req = journeyRequest(request.cacheModel.copy(additionalDeclarationType = Some(STANDARD_PRE_LODGED), parties = parties))

          view(req).getElementsByClass("mainText").get(0) must containText(
            messages("declaration.declarationHolderRequired.mainText.standard_prelodged_other")
          )
        }
      }

      onOccasional { implicit request =>
        "content is correct for that journey " in {
          view.getElementsByClass("mainText").get(0) must containText(messages("declaration.declarationHolderRequired.mainText.occasional.1"))
          view.getElementsByClass("mainText").get(1) must containText(messages("declaration.declarationHolderRequired.mainText.occasional.2"))
        }
      }
    }
  }

  "Declaration Holder Required View back link" should {

    onJourney(STANDARD, SUPPLEMENTARY) { implicit request =>
      "display 'Back' button that links to the 'Authorisation Choice' page" in {
        val backButton = view.getElementById("back-link")
        backButton must containMessage(backCaption)
        backButton must haveHref(routes.AuthorisationProcedureCodeChoiceController.displayPage())
      }
    }

    onOccasional { implicit request =>
      "display 'Back' button that links to the 'Other Parties' page" in {
        val backButton = view.getElementById("back-link")
        backButton must containMessage(backCaption)
        backButton must haveHref(routes.AdditionalActorsSummaryController.displayPage())
      }
    }

    onClearance(aDeclaration(withType(CLEARANCE), withEntryIntoDeclarantsRecords(YesNoAnswers.no))) { implicit request =>
      "EIDR set to false" should {
        "display 'Back' button that links to the 'Consignee Details' page" in {
          val backButton = view.getElementById("back-link")
          backButton must containMessage(backCaption)
          backButton must haveHref(routes.ConsigneeDetailsController.displayPage())
        }
      }
    }

    onClearance(aDeclaration(withType(CLEARANCE), withEntryIntoDeclarantsRecords(YesNoAnswers.yes))) { implicit request =>
      "EIDR set to true" should {
        "display 'Back' button that links to the 'Authorisation Choice' page" in {
          val backButton = view.getElementById("back-link")
          backButton must containMessage(backCaption)
          backButton must haveHref(routes.AuthorisationProcedureCodeChoiceController.displayPage())
        }
      }
    }
  }
}
