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
import forms.declaration.GoodsLocationForm
import models.DeclarationType._
import models.{DeclarationType, Mode}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Call
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.goods_location
import views.tags.ViewTest

@ViewTest
class LocationViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = instanceOf[goods_location]
  private val form: Form[GoodsLocationForm] = GoodsLocationForm.form()
  private def createView(
    mode: Mode = Mode.Normal,
    form: Form[GoodsLocationForm] = form,
    messages: Messages = stubMessages(),
    declarationType: DeclarationType = DeclarationType.STANDARD
  ): Document = page(mode, form)(journeyRequest(declarationType), messages)

  "Location View on empty page" should {

    "have proper messages for labels" in {

      val realMessages = instanceOf[MessagesApi].preferred(journeyRequest())

      realMessages must haveTranslationFor("declaration.goodsLocation.title")
      realMessages must haveTranslationFor("declaration.goodsLocation.hint")
      realMessages must haveTranslationFor("declaration.goodsLocation.code.empty")
      realMessages must haveTranslationFor("declaration.goodsLocation.code.error")
      realMessages must haveTranslationFor("declaration.goodsLocation.help.bodyText")
    }

    val view = createView()

    "display same page title as header" in {

      val viewWithMessage = createView(messages = realMessagesApi.preferred(request))

      viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
    }

    "display section header" in {

      view.getElementById("section-header").text() must include("declaration.summary.locations.header")
    }

    "display header" in {

      view.getElementsByTag("h1").text() mustBe "declaration.goodsLocation.title"
    }

    "display 'Save and continue' button" in {

      val saveButton = view.getElementById("submit")

      saveButton.text() mustBe "site.save_and_continue"
    }

    "display 'Save and return' button" in {

      val saveButton = view.getElementById("submit_and_return")

      saveButton.text() mustBe "site.save_and_come_back_later"
    }
  }

  "Location View for invalid input" should {

    "display error for empty Goods Location code" in {

      val form = GoodsLocationForm.form.fillAndValidate(GoodsLocationForm(""))
      val view = createView(form = form)

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#code")

      view must containErrorElementWithMessage("declaration.goodsLocation.code.empty")
    }

    "display error for incorrect country in the Goods Location code" in {

      val form = GoodsLocationForm.form.fillAndValidate(GoodsLocationForm("XXAU1234567"))
      val view = createView(form = form)

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#code")

      view must containErrorElementWithMessage("declaration.goodsLocation.code.error")
    }

    "display error for incorrect type of location in the Goods Location code" in {

      val form = GoodsLocationForm.form.fillAndValidate(GoodsLocationForm("GBXU12345678"))
      val view = createView(form = form)

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#code")

      view must containErrorElementWithMessage("declaration.goodsLocation.code.error")
    }

    "display error for incorrect qualifier of identification in the Goods Location code" in {

      val form = GoodsLocationForm.form.fillAndValidate(GoodsLocationForm("PLAX12345678"))
      val view = createView(form = form)

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#code")

      view must containErrorElementWithMessage("declaration.goodsLocation.code.error")
    }

    "display error for too short code" in {

      val form = GoodsLocationForm.form.fillAndValidate(GoodsLocationForm("PLAU123"))
      val view = createView(form = form)

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#code")

      view must containErrorElementWithMessage("declaration.goodsLocation.code.error")
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

          backButton must containText("site.back")
          backButton.getElementById("back-link") must haveHref(redirect)
        }
      }
  }
}
