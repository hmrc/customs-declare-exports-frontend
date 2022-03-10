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
import config.AppConfig
import controllers.declaration.routes.UNDangerousGoodsCodeController
import forms.declaration.{CommodityDetails, TaricCodeFirst}
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.taric_code_add_first
import views.tags.ViewTest

@ViewTest
class TaricCodeAddFirstViewSpec extends UnitViewSpec with ExportsTestData with Stubs with CommonMessages with Injector {

  private val appConfig = instanceOf[AppConfig]

  private val itemId = "item1"

  private val form: Form[TaricCodeFirst] = TaricCodeFirst.form()
  private val page = instanceOf[taric_code_add_first]

  private def createView(form: Form[TaricCodeFirst] = form, commodityCode: Option[String] = None): Document =
    page(Mode.Normal, itemId, commodityCode, form)(journeyRequest(), messages)

  "Taric Code Add First View" should {
    val view = createView()

    "display a 'Back' button that links to 'UN Dangerous Goods' page" in {
      val backButton = view.getElementById("back-link")
      backButton.getElementById("back-link") must haveHref(UNDangerousGoodsCodeController.displayPage(Mode.Normal, itemId))
    }

    "display the expected page title" in {
      view.getElementsByTag("h1") must containMessageForElements("declaration.taricAdditionalCodes.addfirst.header")
    }

    "display the expected body" when {

      "a commodity code has been entered" in {
        val commodityCode = "4602191000"
        val body = createView(commodityCode = Some(commodityCode)).getElementsByClass("govuk-body").get(0)

        body.text mustBe messages(
          "declaration.taricAdditionalCodes.addfirst.body",
          messages("declaration.taricAdditionalCodes.addfirst.body.link", commodityCode)
        )
        val href = appConfig.commodityCodeTariffPageUrl.replace(CommodityDetails.placeholder, commodityCode)
        body.child(0) must haveHref(href)
      }

      "a commodity code has not been entered" in {
        val body = createView().getElementsByClass("govuk-body").get(0)

        body must containMessage("declaration.taricAdditionalCodes.addfirst.body", messages("declaration.taricAdditionalCodes.addfirst.body.altlink"))
        body.child(0) must haveHref(appConfig.tradeTariffUrl)
      }
    }

    "display the expected inset paragraph" in {
      val insetText = view.getElementsByClass("govuk-inset-text").get(0)

      insetText.child(0).text mustBe messages("site.example")

      val paragraph = insetText.child(1)

      val linkText = messages("declaration.taricAdditionalCodes.addfirst.inset.text.link")
      paragraph.text mustBe messages("declaration.taricAdditionalCodes.addfirst.inset.text", linkText)
      paragraph.child(0) must haveHref(appConfig.commodityCode9306909000)
    }

    "display a 'Save and continue' button" in {
      val saveButton = view.getElementById("submit")
      saveButton must containMessage(saveAndContinueCaption)
    }

    "display a 'Save and return' button" in {
      val saveAndReturnButton = view.getElementById("submit_and_return")
      saveAndReturnButton must containMessage(saveAndReturnCaption)
    }
  }

  "Taric Code Add First View for invalid input" should {

    "display errors when invalid" in {
      val view = createView(TaricCodeFirst.form().fillAndValidate(TaricCodeFirst(Some("12345678901234567890"))))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#taricCode")

      view must containErrorElementWithMessageKey("declaration.taricAdditionalCodes.error.invalid")
    }

    "display errors when empty" in {
      val view = createView(TaricCodeFirst.form().fillAndValidate(TaricCodeFirst(Some(""))))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#taricCode")

      view must containErrorElementWithMessageKey("declaration.taricAdditionalCodes.error.empty")
    }

  }

  "Taric Code Add First View when filled" should {

    "display data in taric code input" in {

      val view = createView(TaricCodeFirst.form().fill(TaricCodeFirst(Some("ABCD"))))

      view.getElementById("taricCode").attr("value") must be("ABCD")
    }
  }
}
