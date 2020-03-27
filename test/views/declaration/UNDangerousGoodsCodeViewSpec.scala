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

package views.declaration

import base.Injector
import forms.declaration.UNDangerousGoodsCode
import forms.declaration.UNDangerousGoodsCode.AllowedUNDangerousGoodsCodeAnswers
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.un_dangerous_goods_code
import views.tags.ViewTest

@ViewTest
class UNDangerousGoodsCodeViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = instanceOf[un_dangerous_goods_code]
  private val form: Form[UNDangerousGoodsCode] = UNDangerousGoodsCode.form()
  private val itemId = "item1"

  private def createView(mode: Mode = Mode.Normal, form: Form[UNDangerousGoodsCode] = form)(implicit request: JourneyRequest[_]): Document =
    page(mode, itemId, form)(request, stubMessages())

  "UNDangerousGoodsCode View on empty page" should {
    onEveryDeclarationJourney() { implicit request =>
      "have proper messages for labels" in {
        val messages = instanceOf[MessagesApi].preferred(journeyRequest())
        messages must haveTranslationFor("declaration.unDangerousGoodsCode.header")
        messages must haveTranslationFor("declaration.unDangerousGoodsCode.header.hint")
        messages must haveTranslationFor("declaration.unDangerousGoodsCode.hasCode")
        messages must haveTranslationFor("declaration.unDangerousGoodsCode.noCode")
        messages must haveTranslationFor("declaration.unDangerousGoodsCode.label")
      }

      val view = createView()
      "display page title" in {
        view.getElementsByTag("h1").text() must be("declaration.unDangerousGoodsCode.header")
      }

      "display section header" in {
        view.getElementById("section-header").text() must include("supplementary.items")
      }

      "display radio button with Yes option" in {
        view.getElementById("code_yes").attr("value") mustBe AllowedUNDangerousGoodsCodeAnswers.yes
        view.getElementsByAttributeValue("for", "code_yes").text() mustBe "declaration.unDangerousGoodsCode.hasCode"
      }
      "display radio button with No option" in {
        view.getElementById("code_no").attr("value") mustBe AllowedUNDangerousGoodsCodeAnswers.no
        view.getElementsByAttributeValue("for", "code_no").text() mustBe "declaration.unDangerousGoodsCode.noCode"
      }

      "display 'Back' button that links to 'Commodity Details' page" in {

        val backButton = view.getElementById("back-link")

        backButton.text() must be("site.back")
        backButton.getElementById("back-link") must haveHref(
          controllers.declaration.routes.CommodityDetailsController.displayPage(Mode.Normal, itemId)
        )
      }

      "display 'Save and continue' button on page" in {
        val saveButton = view.getElementById("submit")
        saveButton.text() must be("site.save_and_continue")
      }

      "display 'Save and return' button on page" in {
        val saveAndReturnButton = view.getElementById("submit_and_return")
        saveAndReturnButton.text() must be("site.save_and_come_back_later")
      }
    }
  }

  "UNDangerousGoodsCode View for invalid input" should {
    onEveryDeclarationJourney() { implicit request =>
      "display error when code is empty" in {
        val view = createView(form = UNDangerousGoodsCode.form().fillAndValidate(UNDangerousGoodsCode(Some(""))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#dangerousGoodsCode")

        view must containErrorElementWithMessage("declaration.unDangerousGoodsCode.error.empty")
      }

      "display error when code is incorrect" in {
        val view = createView(form = UNDangerousGoodsCode.form().fillAndValidate(UNDangerousGoodsCode(Some("ABC"))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#dangerousGoodsCode")

        view must containErrorElementWithMessage("declaration.unDangerousGoodsCode.error.invalid")
      }
    }
  }
}
