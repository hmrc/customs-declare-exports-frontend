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
import config.AppConfig
import forms.declaration.NactCode
import helpers.views.declaration.CommonMessages
import models.DeclarationType._
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.nact_codes
import views.tags.ViewTest

@ViewTest
class NactCodesViewSpec extends UnitViewSpec with ExportsTestData with Stubs with CommonMessages with Injector {

  private val appConfig = instanceOf[AppConfig]
  private val page = new nact_codes(mainTemplate, appConfig)
  private val itemId = "item1"
  private val realMessages = validatedMessages
  private def createView(form: Form[NactCode], codes: List[NactCode], request: JourneyRequest[_]): Document =
    page(Mode.Normal, itemId, form, codes)(request, realMessages)

  def nactCodeView(request: JourneyRequest[_], taricCode: Option[NactCode] = None, codes: List[NactCode] = List.empty): Unit = {
    val form = taricCode.fold(NactCode.form)(NactCode.form.fill(_))
    val view = createView(form, codes, request)

    "display page title" in {

      view.getElementById("title").text() mustBe realMessages("declaration.nationalAdditionalCode.header")
    }

    "display taric code input field" in {
      val expectedCode = taricCode.map(_.nactCode).getOrElse("")
      view.getElementById(NactCode.nactCodeKey).attr("value") mustBe expectedCode
    }

    "display existing taric codes table" in {
      codes.zipWithIndex.foreach {
        case (code, index) => {
          view.getElementById(s"nactCode-table-row$index-label").text mustBe code.nactCode
          var removeButton = view.getElementById(s"nactCode-table-row$index-remove_button")
          removeButton.text must include(realMessages(removeCaption))
          removeButton.text must include(realMessages("declaration.nationalAdditionalCode.remove.hint", code.nactCode))
        }
      }
    }

    "display 'Add' button on page" in {

      val addButton = view.getElementById("add")
      addButton.text() must include(realMessages(addCaption))
      addButton.text() must include(realMessages("declaration.nationalAdditionalCode.add.hint"))
    }

    "display 'Back' button that links to 'TARIC Code' page" in {
      val backButton = view.getElementById("back-link")

      backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.TaricCodeController.displayPage(Mode.Normal, itemId))
    }

    "display 'Save and continue' button on page" in {

      val saveButton = view.select("#submit")
      saveButton.text() mustBe realMessages(saveAndContinueCaption)
    }
  }

  "NACT Code View on empty page" must {
    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
      val view = createView(NactCode.form, List.empty, request)

      "display page title" in {

        view.getElementById("title").text() mustBe realMessages("declaration.nationalAdditionalCode.header")
      }

      "display taric code input field" in {
        view.getElementById(NactCode.nactCodeKey).attr("value") mustBe empty
      }

      "display 'Add' button on page" in {

        val addButton = view.getElementById("add")
        addButton.text() must include(realMessages(addCaption))
        addButton.text() must include(realMessages("declaration.nationalAdditionalCode.add.hint"))
      }

      "display 'Save and continue' button on page" in {

        val saveButton = view.select("#submit")
        saveButton.text() mustBe realMessages(saveAndContinueCaption)
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
      "display 'Back' button that links to 'TARIC Code' page" in {

        val view = createView(NactCode.form, List.empty, request)

        val backButton = view.getElementById("back-link")

        backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.TaricCodeController.displayPage(Mode.Normal, itemId))
      }
    }
  }

  "NACT Code View on populated page" when {
    val code = NactCode("1234")
    val form = NactCode.form.fill(code)
    val codes = List(NactCode("ABCD"), NactCode("4321"))

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
      val view = createView(form, codes, request)

      "display page title" in {

        view.getElementById("title").text() mustBe realMessages("declaration.nationalAdditionalCode.header")
      }

      "display taric code input field" in {
        val expectedCode = code.nactCode
        view.getElementById(NactCode.nactCodeKey).attr("value") mustBe expectedCode
      }

      "display existing taric codes table" in {
        codes.zipWithIndex.foreach {
          case (code, index) => {
            view.getElementById(s"nactCode-table-row$index-label").text mustBe code.nactCode
            var removeButton = view.getElementById(s"nactCode-table-row$index-remove_button")
            removeButton.text must include(realMessages(removeCaption))
            removeButton.text must include(realMessages("declaration.nationalAdditionalCode.remove.hint", code.nactCode))
          }
        }
      }

      "display 'Add' button on page" in {

        val addButton = view.getElementById("add")
        addButton.text() must include(realMessages(addCaption))
        addButton.text() must include(realMessages("declaration.nationalAdditionalCode.add.hint"))
      }

      "display 'Save and continue' button on page" in {

        val saveButton = view.select("#submit")
        saveButton.text() mustBe realMessages(saveAndContinueCaption)
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
      "display 'Back' button that links to 'TARIC Code' page" in {

        val view = createView(NactCode.form, List.empty, request)

        val backButton = view.getElementById("back-link")

        backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.TaricCodeController.displayPage(Mode.Normal, itemId))
      }
    }

  }
}
