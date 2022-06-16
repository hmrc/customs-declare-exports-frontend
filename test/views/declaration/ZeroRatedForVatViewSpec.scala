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
import controllers.declaration.routes
import forms.declaration.NactCode
import models.DeclarationType._
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import tools.Stubs
import views.components.gds.Styles
import views.declaration.spec.UnitViewSpec
import views.html.declaration.zero_rated_for_vat

class ZeroRatedForVatViewSpec extends UnitViewSpec with Stubs with Injector {

  private val itemId = "itemId"
  private val page = instanceOf[zero_rated_for_vat]
  private def createView(mode: Mode = Mode.Normal, restrictedForZeroVat: Boolean = false)(implicit request: JourneyRequest[_]): Document =
    page(mode, itemId, NactCode.form(), restrictedForZeroVat)

  "Which export procedure are you using Page" must {

    "have proper messages for labels" in {
      messages must haveTranslationFor("declaration.zeroRatedForVat.title")
      messages must haveTranslationFor("declaration.zeroRatedForVat.body.text")
      messages must haveTranslationFor("declaration.zeroRatedForVat.body.linkText")
      messages must haveTranslationFor("declaration.zeroRatedForVat.body.restricted.text")
      messages must haveTranslationFor("declaration.zeroRatedForVat.body.restricted.linkText")
      messages must haveTranslationFor("declaration.zeroRatedForVat.radio.VatZeroRatedYes")
      messages must haveTranslationFor("declaration.zeroRatedForVat.radio.VatZeroRatedReduced")
      messages must haveTranslationFor("declaration.zeroRatedForVat.radio.VatZeroRatedReduced.hint")
      messages must haveTranslationFor("declaration.zeroRatedForVat.radio.VatZeroRatedExempt")
      messages must haveTranslationFor("declaration.zeroRatedForVat.radio.VatZeroRatedExempt.hint")
      messages must haveTranslationFor("declaration.zeroRatedForVat.radio.VatZeroRatedPaid")
      messages must haveTranslationFor("tariff.declaration.item.zeroRatedForVat.common.text")
      messages must haveTranslationFor("tariff.declaration.item.zeroRatedForVat.common.linkText.0")
    }

    onJourney(STANDARD) { implicit request =>
      val view = createView()

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.2")
      }

      "display page title" in {
        view.getElementsByClass(Styles.gdsPageLegend) must containMessageForElements("declaration.zeroRatedForVat.title")
      }

      "display radio buttons" which {
        "have 'VatZeroRatedYes' option" in {
          view.getElementsByAttributeValue("for", "VatZeroRatedYes") must containMessageForElements(
            "declaration.zeroRatedForVat.radio.VatZeroRatedYes"
          )
        }

        "have 'VatZeroRatedReduced' option" in {
          view.getElementsByAttributeValue("for", "VatZeroRatedReduced") must containMessageForElements(
            "declaration.zeroRatedForVat.radio.VatZeroRatedReduced"
          )
        }

        "have 'VatZeroRatedExempt' option" in {
          view.getElementsByAttributeValue("for", "VatZeroRatedExempt") must containMessageForElements(
            "declaration.zeroRatedForVat.radio.VatZeroRatedExempt"
          )
        }

        "have 'VatZeroRatedPaid' option" in {
          view.getElementsByAttributeValue("for", "VatZeroRatedPaid") must containMessageForElements(
            "declaration.zeroRatedForVat.radio.VatZeroRatedPaid"
          )
        }
      }

      "display conditional text when restricted for vat" when {
        "no restrictions on zero vat" in {
          view.getElementsByClass(Styles.gdsPageBody).get(0).text mustBe messages(
            "declaration.zeroRatedForVat.body.text",
            messages("declaration.zeroRatedForVat.body.linkText")
          )
        }
      }

      "display 'Back' button that links to 'Taric' page" in {
        val backButton = view.getElementById("back-link")
        backButton must containMessage("site.back")
        backButton must haveHref(routes.TaricCodeSummaryController.displayPage(Mode.Normal, itemId))
      }

      val createViewWithMode: Mode => Document = mode => createView(mode = mode)
      checkAllSaveButtonsAreDisplayed(createViewWithMode)

    }

    onJourney(STANDARD) { implicit request =>
      val view = createView(restrictedForZeroVat = true)

      "display conditional text" when {
        "restricted for vat" in {
          view.getElementsByClass(Styles.gdsPageBody).get(0).text mustBe messages(
            "declaration.zeroRatedForVat.body.restricted.text",
            messages("declaration.zeroRatedForVat.body.restricted.linkText")
          )
        }
      }

    }

  }
}
