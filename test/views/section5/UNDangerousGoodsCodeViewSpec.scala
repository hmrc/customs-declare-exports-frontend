/*
 * Copyright 2024 HM Revenue & Customs
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

package views.section5

import base.Injector
import controllers.section5.routes.CommodityDetailsController
import forms.section5.UNDangerousGoodsCode
import forms.section5.UNDangerousGoodsCode.{form, AllowedUNDangerousGoodsCodeAnswers}
import models.DeclarationType.STANDARD
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import views.common.PageWithButtonsSpec
import views.html.section5.un_dangerous_goods_code
import views.tags.ViewTest

@ViewTest
class UNDangerousGoodsCodeViewSpec extends PageWithButtonsSpec with Injector {

  val page = instanceOf[un_dangerous_goods_code]

  override val typeAndViewInstance = (STANDARD, page(itemId, form)(_, _))

  def createView(frm: Form[UNDangerousGoodsCode] = form)(implicit request: JourneyRequest[_]): Document =
    page(itemId, frm)(request, messages)

  "UNDangerousGoodsCode View on empty page" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("declaration.unDangerousGoodsCode.header")
      messages must haveTranslationFor("declaration.unDangerousGoodsCode.paragraph")
      messages must haveTranslationFor("declaration.unDangerousGoodsCode.paragraph.link")
      messages must haveTranslationFor("declaration.unDangerousGoodsCode.hasCode")
      messages must haveTranslationFor("declaration.unDangerousGoodsCode.noCode")
      messages must haveTranslationFor("declaration.unDangerousGoodsCode.label")
      messages must haveTranslationFor("declaration.unDangerousGoodsCode.answer.empty")
    }

    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display page title" in {
        view.getElementsByTag("h1") must containMessageForElements("declaration.unDangerousGoodsCode.header")
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.5")
      }

      "display radio button with Yes option" in {
        view.getElementById("code_yes").attr("value") mustBe AllowedUNDangerousGoodsCodeAnswers.yes
        view.getElementsByAttributeValue("for", "code_yes") must containMessageForElements("declaration.unDangerousGoodsCode.hasCode")
      }
      "display radio button with No option" in {
        view.getElementById("code_no").attr("value") mustBe AllowedUNDangerousGoodsCodeAnswers.no
        view.getElementsByAttributeValue("for", "code_no") must containMessageForElements("declaration.unDangerousGoodsCode.noCode")
      }

      "display body text" in {
        val para = view.getElementsByClass("govuk-body")
        val expectedParaText = messages("declaration.unDangerousGoodsCode.paragraph", messages("declaration.unDangerousGoodsCode.paragraph.link"))

        para.get(0).text mustBe expectedParaText
      }

      "display 'Back' button that links to 'Commodity Details' page" in {
        val backButton = view.getElementById("back-link")

        backButton must containMessage("site.backToPreviousQuestion")
        backButton.getElementById("back-link") must haveHref(CommodityDetailsController.displayPage(itemId))
      }

      checkAllSaveButtonsAreDisplayed(createView())
    }
  }

  "UNDangerousGoodsCode View for invalid input" should {
    onEveryDeclarationJourney() { implicit request =>
      "display error when code is empty" in {
        val view = createView(form.fillAndValidate(UNDangerousGoodsCode(Some(""))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#dangerousGoodsCode")

        view must containErrorElementWithMessageKey("declaration.unDangerousGoodsCode.error.empty")
      }

      "display error when code is incorrect" in {
        val view = createView(form.fillAndValidate(UNDangerousGoodsCode(Some("ABC"))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#dangerousGoodsCode")

        view must containErrorElementWithMessageKey("declaration.unDangerousGoodsCode.error.invalid")
      }
    }
  }
}
