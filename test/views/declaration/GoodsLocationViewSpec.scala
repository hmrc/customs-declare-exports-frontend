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

import base.{Injector, TestHelper}
import forms.declaration.GoodsLocationForm
import models.DeclarationType._
import models.{DeclarationType, Mode}
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.api.data.Form
import play.api.mvc.Call
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.goods_location
import views.tags.ViewTest

@ViewTest
class GoodsLocationViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = instanceOf[goods_location]
  private val form: Form[GoodsLocationForm] = GoodsLocationForm.form()

  private def createView(
    mode: Mode = Mode.Normal,
    form: Form[GoodsLocationForm] = form,
    declarationType: DeclarationType = DeclarationType.STANDARD
  ): Document = page(mode, form)(journeyRequest(declarationType), messages)

  "Goods Location View on empty page" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("declaration.goodsLocation.title")
      messages must haveTranslationFor("declaration.goodsLocation.hint")
      messages must haveTranslationFor("declaration.goodsLocation.code.empty")
      messages must haveTranslationFor("declaration.goodsLocation.code.error")
      messages must haveTranslationFor("tariff.declaration.locationOfGoods.clearance.text")
    }

    val view = createView()

    "display same page title as header" in {
      val viewWithMessage = createView()

      viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
    }

    "display section header" in {
      view.getElementById("section-header") must containMessage("declaration.section.3")
    }

    "display header" in {
      view.getElementsByTag("h1") must containMessageForElements("declaration.goodsLocation.title")
    }

    "display goods location expander" in {
      view.getElementsByClass("govuk-details__summary-text").first() must containHtml(messages("declaration.goodsLocation.expander.title"))
    }

    "display tariff expander" in {
      view.getElementsByClass("govuk-details__summary-text").get(1) must containHtml(messages("tariff.expander.title.common"))
    }

    "display 'Save and continue' button" in {
      val saveButton = view.getElementById("submit")

      saveButton must containMessage("site.save_and_continue")
    }

    "display 'Save and return' button" in {
      val saveButton = view.getElementById("submit_and_return")

      saveButton must containMessage("site.save_and_come_back_later")
    }
  }

  "Goods Location View for invalid input" should {

    "display error for empty Goods Location code" in {
      verifyError("", "empty")
    }

    "display error for incorrect country in the Goods Location code" in {
      verifyError("XXAU1234567")
    }

    "display error for incorrect type of location in the Goods Location code" in {
      verifyError("GBXU12345678")
    }

    "display error for incorrect qualifier of identification in the Goods Location code" in {
      verifyError("PLAX12345678")
    }

    "display error for too short code" in {
      verifyError("PLAU123")
    }

    "display error for too long code" in {
      verifyError(s"PLAU${TestHelper.createRandomAlphanumericString(36)}")
    }

    "display error for non-alphanumeric code" in {
      verifyError("PLAX12345678-#@")
    }
  }

  "Goods Location view" should {

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL) { request =>
      behave like viewWithCorrectBackButton(request.declarationType, controllers.declaration.routes.RoutingCountriesSummaryController.displayPage())
    }

    onJourney(SUPPLEMENTARY, CLEARANCE) { request =>
      behave like viewWithCorrectBackButton(request.declarationType, controllers.declaration.routes.DestinationCountryController.displayPage())
    }

    def viewWithCorrectBackButton(declarationType: DeclarationType, redirect: Call): Unit =
      "have correct back-link" when {

        val view = createView(declarationType = declarationType)

        "display 'Back' button that links to correct page" in {

          val backButton = view.getElementById("back-link")

          backButton must containMessage("site.back")
          backButton.getElementById("back-link") must haveHref(redirect)
        }
      }
  }

  private def verifyError(code: String, errorKey: String = "error"): Assertion = {
    val form = GoodsLocationForm.form.fillAndValidate(GoodsLocationForm(code))
    val view = createView(form = form)

    view must haveGovukGlobalErrorSummary
    view must containErrorElementWithTagAndHref("a", "#code")

    view must containErrorElementWithMessageKey(s"declaration.goodsLocation.code.$errorKey")
  }
}
