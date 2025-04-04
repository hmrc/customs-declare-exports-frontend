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
import config.AppConfig
import controllers.section5.routes.{UNDangerousGoodsCodeController, ZeroRatedForVatController}
import forms.section4.NatureOfTransaction.{BusinessPurchase, Construction, Sale}
import forms.section5.NactCodeFirst
import forms.section5.NactCodeFirst.form
import models.DeclarationType._
import models.declaration.ProcedureCodesData.lowValueDeclaration
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import views.common.PageWithButtonsSpec
import views.html.section5.nactCode.nact_code_add_first
import views.tags.ViewTest

@ViewTest
class NactCodeAddFirstViewSpec extends PageWithButtonsSpec with Injector {

  val appConfig = instanceOf[AppConfig]

  val prefix = "declaration.nationalAdditionalCode"

  val page = instanceOf[nact_code_add_first]

  override val typeAndViewInstance = (STANDARD, page(itemId, form)(_, _))

  def createView(frm: Form[NactCodeFirst] = form)(implicit request: JourneyRequest[_]): Document =
    page(itemId, frm)(request, messages)

  "Nact Code Add First View" should {
    val view = createView()

    "display a notification banner" in {
      val banner = view.getElementsByClass("govuk-notification-banner")
      banner.size mustBe 1

      banner.get(0).getElementsByClass("govuk-notification-banner__title").text mustBe messages(s"$prefix.banner.title")
      banner.get(0).getElementsByClass("govuk-notification-banner__content").text mustBe messages(s"$prefix.banner.content")
    }

    "display page title" in {
      view.getElementsByTag("h1") must containMessageForElements(s"$prefix.addfirst.header")
    }

    "display the expected body (the text under page's H1)" in {
      val body = view.getElementsByClass("govuk-body").get(1)
      body.text mustBe messages(s"$prefix.addfirst.body", messages(s"$prefix.addfirst.body.link"))

      body.child(0) must haveHref(appConfig.nationalAdditionalCodes)
    }

    "display the expected radio hint" in {
      val hint = view.getElementsByClass("govuk-hint").get(0)
      hint.text mustBe messages(s"$prefix.addfirst.hint")
    }

    "display 'Back' button" when {

      "STANDARD journey and" when {

        "entered 'sale' in nature-of-transaction" in {
          val view = createView()(journeyRequest(aDeclaration(withType(STANDARD), withNatureOfTransaction(Sale))))
          view.getElementById("back-link") must haveHref(ZeroRatedForVatController.displayPage(itemId))
        }

        "entered 'business purchase' in nature-of-transaction" in {
          val view = createView()(journeyRequest(aDeclaration(withType(STANDARD), withNatureOfTransaction(BusinessPurchase))))
          view.getElementById("back-link") must haveHref(ZeroRatedForVatController.displayPage(itemId))
        }

        "entered other nature-of-transaction" in {
          val view = createView()(journeyRequest(aDeclaration(withType(STANDARD), withNatureOfTransaction(Construction))))
          view.getElementById("back-link") must haveHref(UNDangerousGoodsCodeController.displayPage(itemId))
        }

        "not entered nature-of-transaction" in {
          val view = createView()(journeyRequest(aStandardDeclaration))
          view.getElementById("back-link") must haveHref(UNDangerousGoodsCodeController.displayPage(itemId))
        }
      }

      onJourney(OCCASIONAL, SIMPLIFIED) { implicit request =>
        "for 'low value' declarations" in {
          val item = anItem(withItemId(itemId), withProcedureCodes(additionalProcedureCodes = Seq(lowValueDeclaration)))
          val requestWithCache = journeyRequest(aDeclarationAfter(request.cacheModel, withItems(item)))
          val view = createView()(requestWithCache)
          view.getElementById("back-link") must haveHref(ZeroRatedForVatController.displayPage(itemId))
        }
      }

      onJourney(SUPPLEMENTARY, OCCASIONAL, SIMPLIFIED) { implicit request =>
        s"${request.declarationType} journey" in {
          createView().getElementById("back-link") must haveHref(UNDangerousGoodsCodeController.displayPage(itemId))
        }
      }
    }

    checkAllSaveButtonsAreDisplayed(createView())
  }

  "Nact Code Add First View for invalid input" should {

    "display errors when invalid" in {
      val view = createView(form.fillAndValidate(NactCodeFirst(Some("12345678901234567890"))))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#nactCode")
      view must containErrorElementWithMessageKey(s"$prefix.error.invalid")
    }

    "display errors when empty" in {
      val view = createView(form.fillAndValidate(NactCodeFirst(Some(""))))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#nactCode")
      view must containErrorElementWithMessageKey(s"$prefix.error.empty")
    }
  }

  "Nact Code Add First View when filled" should {
    "display data in nact code input" in {
      val view = createView(form.fill(NactCodeFirst(Some("VATR"))))(journeyRequest())
      view.getElementById("nactCode").attr("value") must be("VATR")
    }
  }
}
