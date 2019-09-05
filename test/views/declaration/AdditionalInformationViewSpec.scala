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

import base.Injector
import controllers.util.{Add, SaveAndContinue, SaveAndReturn}
import forms.declaration.AdditionalInformation
import helpers.views.declaration.{AdditionalInformationMessages, CommonMessages}
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.twirl.api.Html
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.additional_information
import views.tags.ViewTest

@ViewTest
class AdditionalInformationViewSpec
    extends UnitViewSpec with AdditionalInformationMessages with CommonMessages with Stubs with Injector {

  val itemId = "a7sc78"
  private val form: Form[AdditionalInformation] = AdditionalInformation.form()
  private val additionalInformationPage = new additional_information(mainTemplate)
  private def createView(form: Form[AdditionalInformation] = form): Html =
    additionalInformationPage(Mode.Normal, itemId, form, Seq())(request, messages)

  "Additional Information View" should {

    "have a proper messages" in {

      val messages = instanceOf[MessagesApi].preferred(request)

      messages must haveTranslationFor("supplementary.additionalInformation")
      messages must haveTranslationFor("supplementary.additionalInformation.title")
      messages must haveTranslationFor("supplementary.additionalInformation.code")
      messages must haveTranslationFor("supplementary.additionalInformation.item.code")
      messages must haveTranslationFor("supplementary.additionalInformation.code.error")
      messages must haveTranslationFor("supplementary.additionalInformation.code.empty")
      messages must haveTranslationFor("supplementary.additionalInformation.description")
      messages must haveTranslationFor("supplementary.additionalInformation.item.description")
      messages must haveTranslationFor("supplementary.additionalInformation.description.error")
      messages must haveTranslationFor("supplementary.additionalInformation.description.empty")
    }
  }

  "Additional Information View on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() mustBe messages(title)
    }

    "display section header" in {

      createView().getElementById("section-header").text() mustBe "supplementary.summary.yourReferences.header"
    }

    "display empty input with label for Union code" in {

      val view = createView()

      view.getElementById("code-label").text() mustBe messages(code)
      view.getElementById("code").attr("value") mustBe empty
    }

    "display empty input with label for Description" in {

      val view = createView()

      view.getElementById("description-label").text() mustBe messages(description)
      view.getElementById("description").attr("value") mustBe empty
    }

    "display 'Back' button that links to 'Commodity measure' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() mustBe messages(backCaption)
      backButton.attr("href") must endWith(s"/items/$itemId/commodity-measure")
    }

    "display 'Save and continue' button" in {
      val view: Document = createView()
      view must containElement("button").withName(SaveAndContinue.toString)
    }

    "display 'Save and return' button" in {
      val view: Document = createView()
      view must containElement("button").withName(SaveAndReturn.toString)
    }

    "display 'Add' button" in {
      val view: Document = createView()
      view must containElement("button").withName(Add.toString)
    }
  }

  "Additional Information View when filled" should {

    "display data in both inputs" in {

      val view = createView(AdditionalInformation.form.fill(AdditionalInformation("12345", "12345")))

      view.getElementById("code").attr("value") mustBe "12345"
      view.getElementById("description").text() mustBe "12345"

    }

    "display data in code input" in {

      val view = createView(AdditionalInformation.form.fill(AdditionalInformation("12345", "")))

      view.getElementById("code").attr("value") mustBe "12345"
      view.getElementById("description").text() mustBe empty
    }

    "display data in description input" in {

      val view = createView(AdditionalInformation.form.fill(AdditionalInformation("", "12345")))

      view.getElementById("code").attr("value") mustBe empty
      view.getElementById("description").text() mustBe "12345"
    }

    "display one row with data in table" in {

      val view = additionalInformationPage(Mode.Normal, itemId, form, Seq(AdditionalInformation("12345", "12345")))

      view.select("table>tbody>tr>th:nth-child(1)").text() mustBe "12345-12345"

      val removeButton = view.select("table>tbody>tr>th:nth-child(2)>button")
      removeButton.text() mustBe messages(removeCaption)
      removeButton.attr("name") mustBe "Remove"
    }
  }
}
