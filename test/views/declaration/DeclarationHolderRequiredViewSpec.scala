/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.declaration.AuthorisationProcedureCodeChoice.{Choice1007, Choice1040, ChoiceOthers}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import models.DeclarationType._
import models.declaration.Parties
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import services.cache.ExportsTestHelper
import tools.Stubs
import views.components.gds.Styles
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.declarationHolder.declaration_holder_required
import views.tags.ViewTest

@ViewTest
class DeclarationHolderRequiredViewSpec extends UnitViewSpec with ExportsTestHelper with CommonMessages with Stubs with Injector {

  private val form = YesNoAnswer.form()
  private val declarationHolderRequiredPage = instanceOf[declaration_holder_required]

  private val prefix = "declaration.declarationHolderRequired"

  private def view(implicit request: JourneyRequest[_]): Document =
    declarationHolderRequiredPage(form)

  "Declaration Holder Required View" should {

    "have correct message keys" in {
      messages must haveTranslationFor(s"$prefix.tradeTariff.link")
      messages must haveTranslationFor("tariff.declaration.addAuthorisationRequired.common.text")
      messages must haveTranslationFor(s"$prefix.empty")
    }

    "on empty page" when {
      "the additional declaration type is STANDARD_PRE_LODGED and" when {
        "the Authorisation Procedure Code is 1040" should {
          implicit val request = withRequest(STANDARD_PRE_LODGED, withAuthorisationProcedureCodeChoice(Choice1040))

          "display the expected page title" in {
            view.getElementsByClass(Styles.gdsPageLegend) must containMessageForElements(s"$prefix.title.standard.prelodged.1040")
          }

          "not display any inset text" in {
            view.getElementsByClass("govuk-inset-text").size mustBe 0
          }
        }
      }
    }

    "on empty page" should {
      onEveryDeclarationJourney() { implicit request =>
        "display page title" in {
          view.getElementsByClass(Styles.gdsPageLegend) must containMessageForElements(s"$prefix.title")
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

        "display the expected tariff details" in {
          val tariffTitle = view.getElementsByClass("govuk-details__summary-text")
          tariffTitle.text mustBe messages(s"tariff.expander.title.common")

          val tariffDetails = view.getElementsByClass("govuk-details__text").first
          val actualText = removeBlanksIfAnyBeforeDot(tariffDetails.text)

          val prefix = "tariff.declaration.isAuthorisationRequired"
          val expectedText =
            s"""
                ${messages(s"$prefix.1.common.text", messages(s"$prefix.1.common.linkText.0"))}
                ${messages(s"$prefix.2.common.text", messages(s"$prefix.2.common.linkText.0"))}
                ${messages(s"$prefix.3.common.text", messages(s"$prefix.3.common.linkText.0"))}

              """

          val expectedTextWithNoMargin = removeLineBreakIfAny(removeNewLinesIfAny(expectedText).trim)
          actualText mustBe expectedTextWithNoMargin
        }
      }

      onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, CLEARANCE) { implicit request =>
        "display inset text" in {
          val inset = view.getElementsByClass("govuk-inset-text")
          val expected = Seq(
            messages(s"$prefix.inset.para1"),
            messages(s"$prefix.inset.bullet1.text"),
            messages(s"$prefix.inset.bullet2.text"),
            messages(s"$prefix.inset.para2")
          ).mkString(" ")

          inset.get(0) must containText(expected)
        }
      }

      onOccasional { implicit request =>
        "not display any inset text" in {
          view.getElementsByClass("govuk-inset-text").size mustBe 0
        }
      }

      "display main text" that {
        onJourney(SUPPLEMENTARY, SIMPLIFIED, CLEARANCE) { implicit request =>
          "content is correct for that journey " in {
            view.getElementsByClass("govuk-body").get(0) must containText(messages(s"$prefix.body.default"))
          }
        }

        onStandard { implicit request =>
          "'1040' selected then content is correct for that journey " in {
            val parties = Parties(authorisationProcedureCodeChoice = Choice1040)
            val req = journeyRequest(request.cacheModel.copy(additionalDeclarationType = Some(STANDARD_PRE_LODGED), parties = parties))

            view(req).getElementsByClass("govuk-body").get(0) must containText(messages(s"$prefix.body.standard.prelodged.1040"))
          }

          "'1007' selected then content is correct for that journey " in {
            val parties = Parties(authorisationProcedureCodeChoice = Choice1007)
            val req = journeyRequest(request.cacheModel.copy(additionalDeclarationType = Some(STANDARD_PRE_LODGED), parties = parties))

            view(req).getElementsByClass("govuk-body").get(0) must containText(messages(s"$prefix.body.default"))
          }

          "'other' selected then content is correct for that journey " in {
            val parties = Parties(authorisationProcedureCodeChoice = ChoiceOthers)
            val req = journeyRequest(request.cacheModel.copy(additionalDeclarationType = Some(STANDARD_PRE_LODGED), parties = parties))

            view(req).getElementsByClass("govuk-body").get(0) must containText(messages(s"$prefix.body.standard.prelodged.others"))
          }
        }

        onOccasional(aDeclaration(withType(OCCASIONAL), withAdditionalDeclarationType(OCCASIONAL_PRE_LODGED))) { implicit request =>
          "content is correct for OCCASIONAL_PRE_LODGED journey " in {
            view.getElementsByClass("govuk-body").get(0) must containText(messages(s"$prefix.body.occasional.1"))
            view.getElementsByClass("govuk-body").get(1) must containText(messages(s"$prefix.body.occasional.2"))
            view.getElementsByClass("govuk-list--bullet").size mustBe 0
          }
        }

        onOccasional(aDeclaration(withType(OCCASIONAL), withAdditionalDeclarationType(OCCASIONAL_FRONTIER))) { implicit request =>
          "content is correct for OCCASIONAL_FRONTIER journey " in {
            view.getElementsByClass("govuk-body").get(0) must containText(messages(s"$prefix.body.occasional.1"))
            view.getElementsByClass("govuk-body").get(1) must containText(messages(s"$prefix.body.occasional.2"))

            view.getElementsByClass("govuk-list--bullet").size mustBe 1

            val bulletPoints = view.getElementsByClass("govuk-list--bullet").get(0).children
            bulletPoints.get(0) must containMessage(s"$prefix.body.occasional.bullet.1")
            bulletPoints.get(1) must containMessage(s"$prefix.body.occasional.bullet.2")
          }
        }
      }
    }

    "have back link" should {

      onJourney(STANDARD, SUPPLEMENTARY) { implicit request =>
        "display 'Back' button that links to the 'Authorisation Choice' page" in {
          val backButton = view.getElementById("back-link")
          backButton must containMessage(backToPreviousQuestionCaption)
          backButton must haveHref(routes.AuthorisationProcedureCodeChoiceController.displayPage)
        }
      }

      onOccasional { implicit request =>
        "display 'Back' button that links to the 'Other Parties' page" in {
          val backButton = view.getElementById("back-link")
          backButton must containMessage(backToPreviousQuestionCaption)
          backButton must haveHref(routes.AdditionalActorsSummaryController.displayPage)
        }
      }

      onClearance(aDeclaration(withType(CLEARANCE), withEntryIntoDeclarantsRecords(YesNoAnswers.no))) { implicit request =>
        "EIDR set to false" should {
          "display 'Back' button that links to the 'Consignee Details' page" in {
            val backButton = view.getElementById("back-link")
            backButton must containMessage(backToPreviousQuestionCaption)
            backButton must haveHref(routes.ConsigneeDetailsController.displayPage)
          }
        }
      }

      onClearance(aDeclaration(withType(CLEARANCE), withEntryIntoDeclarantsRecords(YesNoAnswers.yes))) { implicit request =>
        "EIDR set to true" should {
          "display 'Back' button that links to the 'Authorisation Choice' page" in {
            val backButton = view.getElementById("back-link")
            backButton must containMessage(backToPreviousQuestionCaption)
            backButton must haveHref(routes.AuthorisationProcedureCodeChoiceController.displayPage)
          }
        }
      }
    }

    "have body text" when {

      "pre-lodged" in {
        implicit val request = withRequest(STANDARD_PRE_LODGED)
        view.getElementsByClass(Styles.gdsPageBody) must containMessageForElements(s"$prefix.body.default")
      }

      "arrived" in {
        implicit val request = withRequest(STANDARD_FRONTIER)
        view.getElementsByClass(Styles.gdsPageBody) must containMessageForElements(s"$prefix.body.standard.arrived")
      }
    }
  }
}
