/*
 * Copyright 2019 HM Revenue & Customs
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

import forms.declaration.CusCode
import helpers.views.declaration.CommonMessages
import models.requests.JourneyRequest
import models.DeclarationType._
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.mvc.Call
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.cus_code
import views.tags.ViewTest

@ViewTest
class CusCodeViewSpec extends UnitViewSpec with ExportsTestData with Stubs with CommonMessages {

  private val page = new cus_code(mainTemplate, minimalAppConfig)
  private val itemId = "item1"
  private val realMessages = validatedMessages
  private def createView(form: Form[CusCode], request: JourneyRequest[_]): Document =
    page(Mode.Normal, itemId, form)(request, realMessages)

  def cusCodeView(journeyRequest: JourneyRequest[_], cusCode: Option[CusCode] = None): Unit = {
    val form = cusCode.fold(CusCode.form)(CusCode.form.fill(_))
    val view = createView(form, journeyRequest)

    "display page title" in {

      view.getElementById("title").text() mustBe realMessages("declaration.cusCode.header")
    }

    "display cus code input field" in {
      val expectedCode = cusCode.flatMap(_.cusCode).getOrElse("")
      view.getElementById(CusCode.cusCodeKey).attr("value") mustBe expectedCode
    }

    "display has cus code field" in {
      view.getElementById(CusCode.hasCusCodeKey).attr("value") mustBe empty
    }

    "display 'Save and continue' button on page" in {

      val saveButton = view.select("#submit")
      saveButton.text() mustBe realMessages(saveAndContinueCaption)
    }
  }

  def backButton(journeyRequest: JourneyRequest[_], call: Call): Unit = {
    val view = createView(CusCode.form, journeyRequest)

    "display back button with correct call" in {
      val backButton = view.getElementById("back-link")

      backButton.text() mustBe realMessages(backCaption)
      backButton must haveHref(call)
    }
  }

  "CUS Code View on empty page" when {

    onEveryDeclarationJourney { request =>
      behave like cusCodeView(request)
    }
  }

  "CUS Code View on populated page" when {
    val dangerousGoodsCode = Some(CusCode(Some("12345678")))

    onEveryDeclarationJourney { request =>
      behave like cusCodeView(request, dangerousGoodsCode)
    }
  }

  "CUS Code View" must {

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
      behave like backButton(request, controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage(Mode.Normal, itemId))
    }

    onClearance { request =>
      behave like backButton(request, controllers.declaration.routes.CommodityDetailsController.displayPage(Mode.Normal, itemId))
    }
  }
}
