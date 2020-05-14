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
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.TaricCode
import helpers.views.declaration.CommonMessages
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.taric_codes
import views.tags.ViewTest

@ViewTest
class TaricCodesViewSpec extends UnitViewSpec with ExportsTestData with Stubs with CommonMessages with Injector {

  private val page = instanceOf[taric_codes]
  private val itemId = "item1"
  private val realMessages = validatedMessages
  private def createView(form: Form[YesNoAnswer], codes: List[TaricCode], request: JourneyRequest[_]): Document =
    page(Mode.Normal, itemId, form, codes)(request, realMessages)

  "Taric Code View on empty page" must {
    onEveryDeclarationJourney() { request =>
      val view = createView(YesNoAnswer.form(), List.empty, request)

      "display page title" in {
        view.getElementsByTag("h1").text() mustBe realMessages("declaration.taricAdditionalCodes.header.plural", "0")
      }

      "display radio button with Yes option" in {
        view.getElementById("code_yes").attr("value") mustBe YesNoAnswers.yes
        view.getElementsByAttributeValue("for", "code_yes").text() mustBe realMessages("site.yes")
      }
      "display radio button with No option" in {
        view.getElementById("code_no").attr("value") mustBe YesNoAnswers.no
        view.getElementsByAttributeValue("for", "code_no").text() mustBe realMessages("site.no")
      }

      "display 'Save and continue' button on page" in {
        val saveButton = view.select("#submit")
        saveButton.text() mustBe realMessages(saveAndContinueCaption)
      }

      "display 'Back' button that links to 'CUS Code' page" in {
        val backButton = view.getElementById("back-link")
        backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.CusCodeController.displayPage(Mode.Normal, itemId))
      }
    }
  }

  "Taric Code View on populated page" when {
    val codes = List(TaricCode("ABCD"), TaricCode("4321"))

    onEveryDeclarationJourney() { request =>
      val view = createView(YesNoAnswer.form, codes, request)

      "display page title" in {

        view.getElementsByTag("h1").text() mustBe realMessages("declaration.taricAdditionalCodes.header.plural", "2")
      }

      "display existing NACT codes table" in {
        codes.zipWithIndex.foreach {
          case (code, index) => {
            view.getElementById(s"taricCode-table-row$index-label").text mustBe code.taricCode
            var removeButton = view.getElementById(s"taricCode-table-row$index-remove_button")
            removeButton.text must include(realMessages(removeCaption))
            removeButton.text must include(realMessages("declaration.taricAdditionalCodes.remove.hint", code.taricCode))
          }
        }
      }
    }
  }

  "Taric Code View with single code" when {
    val codes = List(TaricCode("ABCD"))

    onEveryDeclarationJourney() { request =>
      val view = createView(YesNoAnswer.form, codes, request)

      "display page title" in {

        view.getElementsByTag("h1").text() mustBe realMessages("declaration.taricAdditionalCodes.header.singular")
      }
    }
  }

}
