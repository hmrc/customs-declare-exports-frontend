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

package views.declaration

import base.Injector
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.NactCode
import forms.declaration.NatureOfTransaction.{BusinessPurchase, HouseRemoval, Sale}
import models.DeclarationType._
import models.{DeclarationType, Mode}
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.nact_codes
import views.tags.ViewTest

@ViewTest
class NactCodesViewSpec extends UnitViewSpec with ExportsTestData with Stubs with CommonMessages with Injector {

  private val page = instanceOf[nact_codes]
  private val itemId = "item1"
  private def createView(form: Form[YesNoAnswer], codes: List[NactCode])(implicit request: JourneyRequest[_]): Document =
    page(Mode.Normal, itemId, form, codes)(request, messages)

  "NACT Code View on empty page" must {
    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      val view = createView(YesNoAnswer.form(), List.empty)

      "display page title" in {
        view.getElementsByTag("h1") must containMessageForElements("declaration.nationalAdditionalCode.header.plural", "0")
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
        val saveButton = view.select("#submit")
        saveButton must containMessageForElements(saveAndContinueCaption)
      }

    }
    onJourney(SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      val view = createView(YesNoAnswer.form(), List.empty)
      "display 'Back' button that links to 'TARIC Code' page" in {
        val backButton = view.getElementById("back-link")
        backButton.getElementById("back-link") must haveHref(
          controllers.declaration.routes.TaricCodeSummaryController.displayPage(Mode.Normal, itemId)
        )
      }
    }
    onJourney(STANDARD) { implicit request =>
      "display 'Back' button" when {
        "sale answered to nature-of-transaction" in {

          val view = createView(YesNoAnswer.form(), List.empty)(
            journeyRequest(aDeclaration(withType(DeclarationType.STANDARD), withNatureOfTransaction(Sale)))
          )

          val backButton = view.getElementById("back-link")
          backButton.getElementById("back-link") must haveHref(
            controllers.declaration.routes.ZeroRatedForVatController.displayPage(Mode.Normal, itemId)
          )
        }
        "business purchase answered to nature-of-transaction" in {

          val view = createView(YesNoAnswer.form(), List.empty)(
            journeyRequest(aDeclaration(withType(DeclarationType.STANDARD), withNatureOfTransaction(BusinessPurchase)))
          )

          val backButton = view.getElementById("back-link")
          backButton.getElementById("back-link") must haveHref(
            controllers.declaration.routes.ZeroRatedForVatController.displayPage(Mode.Normal, itemId)
          )
        }
        "sale is not answer to nature-of-transaction" in {

          val view = createView(YesNoAnswer.form(), List.empty)(
            journeyRequest(aDeclaration(withType(DeclarationType.STANDARD), withNatureOfTransaction(HouseRemoval)))
          )

          val backButton = view.getElementById("back-link")
          backButton.getElementById("back-link") must haveHref(
            controllers.declaration.routes.TaricCodeSummaryController.displayPage(Mode.Normal, itemId)
          )
        }
        "nature-of-transaction is empty" in {

          val view = createView(YesNoAnswer.form(), List.empty)

          val backButton = view.getElementById("back-link")
          backButton.getElementById("back-link") must haveHref(
            controllers.declaration.routes.TaricCodeSummaryController.displayPage(Mode.Normal, itemId)
          )
        }
      }
    }
  }

  "NACT Code View on populated page" when {
    val codes = List(NactCode("ABCD"), NactCode("4321"))

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      val view = createView(YesNoAnswer.form(), codes)

      "display page title" in {

        view.getElementsByTag("h1") must containMessageForElements("declaration.nationalAdditionalCode.header.plural", "2")
      }

      "display table headers" in {
        view.getElementsByTag("th").get(0).text() mustBe messages("declaration.nationalAdditionalCode.table.header")
      }

      "have visually hidden header for Remove links" in {
        view.getElementsByTag("th").get(1).text() mustBe messages("site.remove.header")
      }

      "display existing NACT codes table" in {
        codes.zipWithIndex.foreach { case (code, index) =>
          view.getElementById(s"nactCode-table-row$index-label").text mustBe code.nactCode
          val removeButton = view.getElementById(s"nactCode-table-row$index-remove_button")
          removeButton must containMessage(removeCaption)
          removeButton must containMessage("declaration.nationalAdditionalCode.remove.hint", code.nactCode)
        }
      }
    }
  }

  "NACT Code View with single code" when {
    val codes = List(NactCode("ABCD"))

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      val view = createView(YesNoAnswer.form(), codes)

      "display page title" in {

        view.getElementsByTag("h1") must containMessageForElements("declaration.nationalAdditionalCode.header.singular")
      }
    }
  }
}
