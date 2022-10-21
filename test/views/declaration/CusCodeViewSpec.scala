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
import controllers.declaration.routes.UNDangerousGoodsCodeController
import forms.declaration.CusCode
import forms.declaration.CusCode.{form, AllowedCUSCodeAnswers}
import models.DeclarationType.{OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.cus_code
import views.tags.ViewTest

@ViewTest
class CusCodeViewSpec extends PageWithButtonsSpec with Injector {

  val page = instanceOf[cus_code]

  override val typeAndViewInstance = (STANDARD, page(itemId, form)(_, _))

  def createView(frm: Form[CusCode] = form())(implicit request: JourneyRequest[_]): Document =
    page(itemId, frm)(request, messages)

  "CusCode View on empty page" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("declaration.cusCode.header")
      messages must haveTranslationFor("declaration.cusCode.paragraph")
      messages must haveTranslationFor("declaration.cusCode.hasCode")
      messages must haveTranslationFor("declaration.cusCode.noCode")
      messages must haveTranslationFor("declaration.cusCode.label")
      messages must haveTranslationFor("declaration.cusCode.answer.empty")
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      val view = createView()

      "display page title" in {
        view.getElementsByTag("h1") must containMessageForElements("declaration.cusCode.header")
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.5")
      }

      "display radio button with Yes option" in {
        view.getElementById("code_yes").attr("value") mustBe AllowedCUSCodeAnswers.yes
        view.getElementsByAttributeValue("for", "code_yes") must containMessageForElements("declaration.cusCode.hasCode")
      }

      "display radio button with No option" in {
        view.getElementById("code_no").attr("value") mustBe AllowedCUSCodeAnswers.no
        view.getElementsByAttributeValue("for", "code_no") must containMessageForElements("declaration.cusCode.noCode")
      }

      "display link" in {
        view.getElementsByClass("govuk-body").get(0).text() mustBe messages(
          "declaration.cusCode.paragraph",
          messages("declaration.cusCode.paragraph.link")
        )
      }

      "display 'Back' button that links to 'UN Dangerous Goods' page" in {
        val backButton = view.getElementById("back-link")

        backButton must containMessage("site.backToPreviousQuestion")
        backButton.getElementById("back-link") must haveHref(UNDangerousGoodsCodeController.displayPage(itemId))
      }

      checkAllSaveButtonsAreDisplayed(createView())
    }
  }

  "CusCode View for invalid input" should {
    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display error when code is empty" in {
        val view = createView(form().fillAndValidate(CusCode(Some(""))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#cusCode")

        view must containErrorElementWithMessageKey("declaration.cusCode.error.empty")
      }

      "display error when code is incorrect" in {
        val view = createView(form().fillAndValidate(CusCode(Some("ABC!!!"))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#cusCode")

        view must containErrorElementWithMessageKey("declaration.cusCode.error.length")
        view must containErrorElementWithMessageKey("declaration.cusCode.error.specialCharacters")
      }
    }
  }
}
