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

package views.section5

import base.Injector
import controllers.section5.routes.{UNDangerousGoodsCodeController, ZeroRatedForVatController}
import forms.common.YesNoAnswer.{form, YesNoAnswers}
import forms.section4.NatureOfTransaction.{BusinessPurchase, HouseRemoval, Sale}
import forms.section5.NactCode
import models.DeclarationType._
import models.declaration.ProcedureCodesData.lowValueDeclaration
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import views.common.UnitViewSpec
import views.html.section5.nactCode.nact_codes
import views.tags.ViewTest

@ViewTest
class NactCodesViewSpec extends UnitViewSpec with Injector {

  val page = instanceOf[nact_codes]

  def createView(codes: List[NactCode] = List.empty)(implicit request: JourneyRequest[_]): Document =
    page(itemId, form(), codes)(request, messages)

  "NACT Code View on empty page" must {
    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      val view = createView()

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
      "display 'Back' button that links to the 'UN Dangerous Goods Code' page" in {
        createView().getElementById("back-link") must haveHref(UNDangerousGoodsCodeController.displayPage(itemId))
      }
    }

    "display 'Back' button" when {

      "entered 'sale' in nature-of-transaction" in {
        val view = createView()(journeyRequest(aDeclaration(withType(STANDARD), withNatureOfTransaction(Sale))))
        view.getElementById("back-link") must haveHref(ZeroRatedForVatController.displayPage(itemId))
      }

      "entered 'business purchase' in nature-of-transaction" in {
        val view = createView()(journeyRequest(aDeclaration(withType(STANDARD), withNatureOfTransaction(BusinessPurchase))))
        view.getElementById("back-link") must haveHref(ZeroRatedForVatController.displayPage(itemId))
      }

      "entered other nature-of-transaction" in {
        val view = createView()(journeyRequest(aDeclaration(withType(STANDARD), withNatureOfTransaction(HouseRemoval))))
        view.getElementById("back-link") must haveHref(UNDangerousGoodsCodeController.displayPage(itemId))
      }

      "not entered nature-of-transaction" in {
        createView().getElementById("back-link") must haveHref(UNDangerousGoodsCodeController.displayPage(itemId))
      }

      "declaration is a 'low value' one" in {
        occasionalAndSimplified.foreach { declarationType =>
          val item = anItem(withItemId(itemId), withProcedureCodes(additionalProcedureCodes = Seq(lowValueDeclaration)))
          val requestWithCache = journeyRequest(aDeclaration(withType(declarationType), withItems(item)))
          val view = createView()(requestWithCache)
          view.getElementById("back-link") must haveHref(ZeroRatedForVatController.displayPage(itemId))
        }
      }
    }
  }

  "NACT Code View on populated page" when {
    val codes = List(NactCode("ABCD"), NactCode("4321"))

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      val view = createView(codes)

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
      val view = createView(codes)

      "display page title" in {
        view.getElementsByTag("h1") must containMessageForElements("declaration.nationalAdditionalCode.header.singular")
      }
    }
  }
}
