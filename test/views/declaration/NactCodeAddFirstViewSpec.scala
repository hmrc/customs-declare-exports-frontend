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
import controllers.declaration.routes.{TaricCodeSummaryController, ZeroRatedForVatController}
import forms.declaration.NactCodeFirst
import forms.declaration.NatureOfTransaction.{BusinessPurchase, Construction, Sale}
import models.requests.JourneyRequest
import models.{DeclarationType, Mode}
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.nact_code_add_first
import views.tags.ViewTest

@ViewTest
class NactCodeAddFirstViewSpec extends UnitViewSpec with ExportsTestData with Stubs with CommonMessages with Injector {

  private val appConfig = instanceOf[AppConfig]

  private val prefix = "declaration.nationalAdditionalCode"

  private val itemId = "item1"
  private val form: Form[NactCodeFirst] = NactCodeFirst.form()
  private val page = instanceOf[nact_code_add_first]

  private def createView(form: Form[NactCodeFirst] = form, mode: Mode = Mode.Normal)(implicit request: JourneyRequest[_]): Document =
    page(mode, itemId, form)(request, messages)

  "Nact Code Add First View" should {
    val view = createView()(journeyRequest())

    "display page title" in {
      view.getElementsByTag("h1") must containMessageForElements(s"$prefix.addfirst.header")
    }

    "display the expected body (the text under page's H1)" in {
      val body = view.getElementsByClass("govuk-body").get(0)
      body.text mustBe messages(s"$prefix.addfirst.body", messages(s"$prefix.addfirst.body.link"))

      body.child(0) must haveHref(appConfig.nationalAdditionalCodes)
    }

    "display the expected radio hint" in {
      val hint = view.getElementsByClass("govuk-hint").get(0)
      hint.text mustBe messages(s"$prefix.addfirst.hint")
    }

    "display 'Back' button" when {

      "STANDARD journey" when {
        "answered sale in nature of transaction" in {

          val view = createView()(journeyRequest(aDeclaration(withType(DeclarationType.STANDARD), withNatureOfTransaction(Sale))))

          val backLink = view.getElementById("back-link")
          backLink.getElementById("back-link") must haveHref(ZeroRatedForVatController.displayPage(Mode.Normal, itemId))
        }
        "answered business purchase nature of transaction" in {

          val view = createView()(journeyRequest(aDeclaration(withType(DeclarationType.STANDARD), withNatureOfTransaction(BusinessPurchase))))

          val backLink = view.getElementById("back-link")
          backLink.getElementById("back-link") must haveHref(ZeroRatedForVatController.displayPage(Mode.Normal, itemId))
        }
        "answered other nature of transaction" in {

          val view = createView()(journeyRequest(aDeclaration(withType(DeclarationType.STANDARD), withNatureOfTransaction(Construction))))

          val backLink = view.getElementById("back-link")
          backLink.getElementById("back-link") must haveHref(TaricCodeSummaryController.displayPage(Mode.Normal, itemId))
        }
        "not answered nature of transaction" in {

          val view = createView()(journeyRequest(aDeclaration(withType(DeclarationType.STANDARD))))

          val backLink = view.getElementById("back-link")
          backLink.getElementById("back-link") must haveHref(TaricCodeSummaryController.displayPage(Mode.Normal, itemId))
        }
      }

      onJourney(DeclarationType.SUPPLEMENTARY, DeclarationType.OCCASIONAL, DeclarationType.SIMPLIFIED) { implicit request =>
        s"${request.declarationType} journey" in {

          val backLink = createView().getElementById("back-link")
          backLink.getElementById("back-link") must haveHref(TaricCodeSummaryController.displayPage(Mode.Normal, itemId))
        }
      }
    }

    val createViewWithMode: Mode => Document = mode => createView(mode = mode)(journeyRequest())
    checkAllSaveButtonsAreDisplayed(createViewWithMode)
  }

  "Nact Code Add First View for invalid input" should {

    implicit val request = journeyRequest()

    "display errors when invalid" in {
      val view = createView(NactCodeFirst.form().fillAndValidate(NactCodeFirst(Some("12345678901234567890"))))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#nactCode")
      view must containErrorElementWithMessageKey(s"$prefix.error.invalid")
    }

    "display errors when empty" in {
      val view = createView(NactCodeFirst.form().fillAndValidate(NactCodeFirst(Some(""))))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#nactCode")
      view must containErrorElementWithMessageKey(s"$prefix.error.empty")
    }
  }

  "Nact Code Add First View when filled" should {
    "display data in nact code input" in {
      val view = createView(NactCodeFirst.form().fill(NactCodeFirst(Some("VATR"))))(journeyRequest())
      view.getElementById("nactCode").attr("value") must be("VATR")
    }
  }
}
