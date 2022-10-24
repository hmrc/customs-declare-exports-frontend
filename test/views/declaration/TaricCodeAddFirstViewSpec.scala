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
import forms.declaration.TaricCodeFirst.form
import forms.declaration.{CommodityDetails, TaricCodeFirst}
import models.DeclarationType.STANDARD
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.mvc.AnyContent
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.taric_code_add_first
import views.tags.ViewTest

@ViewTest
class TaricCodeAddFirstViewSpec extends PageWithButtonsSpec with Injector {

  val appConfig = instanceOf[AppConfig]

  private def request(commodityCode: String): JourneyRequest[AnyContent] = {
    val item = anItem(withItemId(itemId), withCommodityDetails(CommodityDetails(Some(commodityCode), None)))
    withRequestOfType(STANDARD, withItem(item))
  }

  val page = instanceOf[taric_code_add_first]

  override val typeAndViewInstance = (STANDARD, page(itemId, form())(_, _))

  def createView(frm: Form[TaricCodeFirst] = form())(implicit request: JourneyRequest[_]): Document =
    page(itemId, frm)(request, messages(request))

  "Taric Code Add First View" should {
    val view = createView()

    "display a 'Back' button that links to 'UN Dangerous Goods' page" in {
      val backButton = view.getElementById("back-link")
      backButton.getElementById("back-link") must haveHref(UNDangerousGoodsCodeController.displayPage(itemId))
    }

    "display the expected page title" in {
      view.getElementsByTag("h1") must containMessageForElements("declaration.taricAdditionalCodes.addfirst.header")
    }

    "display the expected body" when {

      "a commodity code of 10-digits has been entered" in {
        val commodityCode = "4602191000"
        val body = createView()(request(commodityCode)).getElementsByClass("govuk-body").get(0)

        val expectedLinkText = messages("declaration.taricAdditionalCodes.addfirst.body.link", commodityCode)
        val expectedHref = appConfig.commodityCodeTariffPageUrl.replace(CommodityDetails.placeholder, commodityCode)

        body.text mustBe messages("declaration.taricAdditionalCodes.addfirst.body", expectedLinkText)
        body.child(0) must haveHref(expectedHref)
      }

      "a commodity code of 8-digits has been entered" in {
        val commodityCode = "46021910"
        val body = createView()(request(commodityCode)).getElementsByClass("govuk-body").get(0)

        val expectedLinkText = messages("declaration.taricAdditionalCodes.addfirst.body.link", commodityCode)
        val expectedHref = appConfig.commodityCodeTariffPageUrl.replace(CommodityDetails.placeholder, s"${commodityCode}00")

        body.text mustBe messages("declaration.taricAdditionalCodes.addfirst.body", expectedLinkText)
        body.child(0) must haveHref(expectedHref)
      }

      "a commodity code has not been entered" in {
        val body = view.getElementsByClass("govuk-body").get(0)

        val altlink = messages("declaration.taricAdditionalCodes.addfirst.body.altlink")
        body must containMessage("declaration.taricAdditionalCodes.addfirst.body", altlink)
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

    checkAllSaveButtonsAreDisplayed(createView())
  }

  "Taric Code Add First View for invalid input" should {

    "display errors when invalid" in {
      val view = createView(form().fillAndValidate(TaricCodeFirst(Some("12345678901234567890"))))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#taricCode")
      view must containErrorElementWithMessageKey("declaration.taricAdditionalCodes.error.invalid")
    }

    "display errors when empty" in {
      val view = createView(form().fillAndValidate(TaricCodeFirst(Some(""))))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#taricCode")
      view must containErrorElementWithMessageKey("declaration.taricAdditionalCodes.error.empty")
    }

  }

  "Taric Code Add First View when filled" should {
    "display data in taric code input" in {
      val view = createView(form().fill(TaricCodeFirst(Some("ABCD"))))
      view.getElementById("taricCode").attr("value") must be("ABCD")
    }
  }
}
