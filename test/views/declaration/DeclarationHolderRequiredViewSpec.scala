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
import config.AppConfig
import controllers.declaration.routes
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import helpers.views.declaration.CommonMessages
import models.requests.JourneyRequest
import models.{DeclarationType, Mode}
import org.jsoup.nodes.Document
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.components.gds.Styles
import views.declaration.spec.UnitViewSpec
import views.html.declaration.declarationHolder.declaration_holder_required
import views.tags.ViewTest

@ViewTest
class DeclarationHolderRequiredViewSpec extends UnitViewSpec with ExportsTestData with CommonMessages with Stubs with Injector {
  private val form = YesNoAnswer.form()
  private val declarationHolderRequiredPage = instanceOf[declaration_holder_required]
  private def view(implicit request: JourneyRequest[_]): Document =
    declarationHolderRequiredPage(Mode.Normal, form)

  "Declaration Holder Required View on empty page" should {

    "have correct message keys" in {

      messages must haveTranslationFor("declaration.declarationHolderRequired.title")
      messages must haveTranslationFor("declaration.declarationHolderRequired.hint.1")
      messages must haveTranslationFor("declaration.declarationHolderRequired.hint.2")
      messages must haveTranslationFor("declaration.declarationHolderRequired.tradeTariff.link")
      messages must haveTranslationFor("declaration.declarationHolderRequired.help.bodyText")
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

      "display page hints" in {
        view.getElementById("declaration-holder-required-hint1") must containMessage("declaration.declarationHolderRequired.hint.1")

        val hint2 = view.getElementById("declaration-holder-required-hint2")
        hint2.childNodeSize() mustBe 3
        hint2.child(0) must haveHref(instanceOf[AppConfig].tradeTariffUrl)
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
  }

  "Declaration Holder Required View back link" should {

    onJourney(DeclarationType.OCCASIONAL, DeclarationType.SIMPLIFIED, DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY) { implicit request =>
      "display 'Back' button that links to the 'Additional Actors Summary' page" in {
        val backButton = view.getElementById("back-link")
        backButton must containMessage(backCaption)
        backButton must haveHref(routes.AdditionalActorsSummaryController.displayPage(Mode.Normal))
      }
    }

    onClearance { implicit request =>
      "display 'Back' button that links to the 'Consignee Details' page" in {
        val backButton = view.getElementById("back-link")
        backButton must containMessage(backCaption)
        backButton must haveHref(routes.ConsigneeDetailsController.displayPage(Mode.Normal))
      }
    }
  }
}
