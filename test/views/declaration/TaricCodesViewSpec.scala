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
import forms.declaration.TaricCode
import helpers.views.declaration.CommonMessages
import models.DeclarationType.DeclarationType
import models.{DeclarationType, Mode}
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.taric_codes
import views.tags.ViewTest
import config.AppConfig

@ViewTest
class TaricCodesViewSpec extends UnitViewSpec with ExportsTestData with Stubs with CommonMessages with Injector {

  private val appConfig = instanceOf[AppConfig]
  private val page = new taric_codes(mainTemplate, appConfig)
  private val itemId = "item1"
  private val realMessages = validatedMessages
  private def createView(declarationType: DeclarationType, form: Form[TaricCode], codes: List[TaricCode]): Document =
    page(Mode.Normal, itemId, form, codes)(journeyRequest(declarationType), realMessages)

  def taricCodeView(declarationType: DeclarationType, taricCode: Option[TaricCode] = None, codes: List[TaricCode] = List.empty): Unit = {
    val form = taricCode.fold(TaricCode.form)(TaricCode.form.fill(_))
    val view = createView(declarationType, form, codes)

    "display page title" in {

      view.getElementById("title").text() mustBe realMessages("declaration.taricAdditionalCodes.header")
    }

    "display taric code input field" in {
      val expectedCode = taricCode.map(_.taricCode).getOrElse("")
      view.getElementById(TaricCode.taricCodeKey).attr("value") mustBe expectedCode
    }

    "display existing taric codes table" in {
      codes.zipWithIndex.foreach {
        case (code, index) => {
          view.getElementById(s"taricCode-table-row$index-label").text mustBe code.taricCode
          var removeButton = view.getElementById(s"taricCode-table-row$index-remove_button")
          removeButton.text must include(realMessages(removeCaption))
          removeButton.text must include(realMessages("declaration.taricAdditionalCodes.remove.hint", code.taricCode))
        }
      }
    }

    "display 'Add' button on page" in {

      val addButton = view.getElementById("add")
      addButton.text() must include(realMessages(addCaption))
      addButton.text() must include(realMessages("declaration.taricAdditionalCodes.add.hint"))
    }

    "display 'Back' button that links to 'CUS Code' page" in {
      val backButton = view.getElementById("back-link")

      backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.CusCodeController.displayPage(Mode.Normal, itemId))
    }

    "display 'Save and continue' button on page" in {

      val saveButton = view.select("#submit")
      saveButton.text() mustBe realMessages(saveAndContinueCaption)
    }
  }

  "Taric Code View on empty page" when {

    for (decType <- DeclarationType.values) {
      s"we are on $decType journey" should {
        behave like taricCodeView(decType)
      }
    }
  }

  "Taric Code View on populated page" when {
    val taricCode = Some(TaricCode("1234"))
    val existingCodes = List(TaricCode("ABCD"), TaricCode("4321"))

    for (decType <- DeclarationType.values) {
      s"we are on $decType journey" should {
        behave like taricCodeView(decType, taricCode, existingCodes)
      }
    }
  }
}
