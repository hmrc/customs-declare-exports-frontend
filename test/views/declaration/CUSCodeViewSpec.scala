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

import forms.declaration.CUSCode
import helpers.views.declaration.CommonMessages
import models.DeclarationType.DeclarationType
import models.{DeclarationType, Mode}
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.cus_code
import views.tags.ViewTest

@ViewTest
class CUSCodeViewSpec extends UnitViewSpec with ExportsTestData with Stubs with CommonMessages {

  private val page = new cus_code(mainTemplate, minimalAppConfig)
  private val itemId = "item1"
  private val realMessages = validatedMessages
  private def createView(declarationType: DeclarationType = DeclarationType.STANDARD, form: Form[CUSCode]): Document =
    page(Mode.Normal, itemId, form)(journeyRequest(declarationType), realMessages)

  def cusCodeView(declarationType: DeclarationType, cusCode: Option[CUSCode] = None): Unit = {
    val form = cusCode.fold(CUSCode.form)(CUSCode.form.fill(_))
    val view = createView(declarationType, form)

    "display page title" in {

      view.getElementById("title").text() mustBe realMessages("declaration.cusCode.header")
    }

    "display cus code input field" in {
      val expectedCode = cusCode.flatMap(_.cusCode).getOrElse("")
      view.getElementById(CUSCode.cusCodeKey).attr("value") mustBe expectedCode
    }

    "display has cus code field" in {
      view.getElementById(CUSCode.hasCusCodeKey).attr("value") mustBe empty
    }

    "display 'Back' button that links to 'UN Dangerous Goods Code' page" in {
      val backButton = view.getElementById("back-link")

      backButton.getElementById("back-link") must haveHref(
        controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage(Mode.Normal, itemId)
      )
    }

    "display 'Save and continue' button on page" in {

      val saveButton = view.select("#submit")
      saveButton.text() mustBe realMessages(saveAndContinueCaption)
    }
  }

  "UN Dangerous Goods Code View on empty page" when {
    "we are on Standard journey" should {
      behave like cusCodeView(DeclarationType.STANDARD)
    }
    "we are on Supplementary journey" should {
      behave like cusCodeView(DeclarationType.SUPPLEMENTARY)
    }
    "we are on Simplified journey" should {
      behave like cusCodeView(DeclarationType.SIMPLIFIED)
    }
  }

  "UN Dangerous Goods Code View on populated page" when {
    val dangerousGoodsCode = Some(CUSCode(Some("12345678")))

    "we are on Standard journey" should {
      behave like cusCodeView(DeclarationType.STANDARD, dangerousGoodsCode)
    }
    "we are on Supplementary journey" should {
      behave like cusCodeView(DeclarationType.SUPPLEMENTARY, dangerousGoodsCode)
    }
    "we are on Simplified journey" should {
      behave like cusCodeView(DeclarationType.SIMPLIFIED, dangerousGoodsCode)
    }
  }
}
