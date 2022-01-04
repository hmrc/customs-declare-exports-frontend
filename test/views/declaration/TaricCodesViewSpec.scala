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
import forms.declaration.TaricCode
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.taric_codes
import views.tags.ViewTest

@ViewTest
class TaricCodesViewSpec extends UnitViewSpec with ExportsTestData with Stubs with CommonMessages with Injector {

  private val page = instanceOf[taric_codes]
  private val itemId = "item1"
  private def createView(form: Form[YesNoAnswer], codes: List[TaricCode])(implicit request: JourneyRequest[_]): Document =
    page(Mode.Normal, itemId, form, codes)(request, messages)

  "Taric Code View on empty page" must {
    onEveryDeclarationJourney() { implicit request =>
      val view = createView(YesNoAnswer.form(), List.empty)

      "display page title" in {
        view.getElementsByTag("h1") must containMessageForElements("declaration.taricAdditionalCodes.header.plural", "0")
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

      "display 'Back' button that links to 'UN Dangerous Goods' page" in {
        val backButton = view.getElementById("back-link")
        backButton.getElementById("back-link") must haveHref(
          controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage(Mode.Normal, itemId)
        )
      }
    }
  }

  "Taric Code View on populated page" when {
    val codes = List(TaricCode("ABCD"), TaricCode("4321"))

    onEveryDeclarationJourney() { implicit request =>
      val view = createView(YesNoAnswer.form(), codes)

      "display page title" in {

        view.getElementsByTag("h1") must containMessageForElements("declaration.taricAdditionalCodes.header.plural", "2")
      }

      "display table headers" in {
        view.getElementsByTag("th").get(0).text() mustBe messages("declaration.taricAdditionalCodes.table.header")
      }

      "have visually hidden header for Remove links" in {
        view.getElementsByTag("th").get(1).text() mustBe messages("site.remove.header")
      }

      "display existing NACT codes table" in {
        codes.zipWithIndex.foreach {
          case (code, index) => {
            view.getElementById(s"taricCode-table-row$index-label").text mustBe code.taricCode
            val removeButton = view.getElementById(s"taricCode-table-row$index-remove_button")
            removeButton.text must include(messages(removeCaption))
            removeButton.text must include(messages("declaration.taricAdditionalCodes.remove.hint", code.taricCode))
          }
        }
      }
    }
  }

  "Taric Code View with single code" when {
    val codes = List(TaricCode("ABCD"))

    onEveryDeclarationJourney() { implicit request =>
      val view = createView(YesNoAnswer.form(), codes)

      "display page title" in {

        view.getElementsByTag("h1") must containMessageForElements("declaration.taricAdditionalCodes.header.singular")
      }
    }
  }

}
