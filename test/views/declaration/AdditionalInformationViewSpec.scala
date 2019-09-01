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

import controllers.util.{Add, SaveAndContinue, SaveAndReturn}
import forms.declaration.AdditionalInformation
import helpers.views.declaration.{AdditionalInformationMessages, CommonMessages}
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.AppViewSpec
import views.html.declaration.additional_information
import views.tags.ViewTest

@ViewTest
class AdditionalInformationViewSpec extends AppViewSpec with AdditionalInformationMessages with CommonMessages {

  private val form: Form[AdditionalInformation] = AdditionalInformation.form()
  private val additionalInformationPage = app.injector.instanceOf[additional_information]
  private def createView(form: Form[AdditionalInformation] = form): Html =
    additionalInformationPage(Mode.Normal, itemId, form, Seq())(fakeRequest, messages)

  /*
   * Moved all errors tests to AdditionalInformationControllerSpec,
   * as the logic depends on which button we will press (we can't emulate it
   * at view tests)
   */

  "Additional Information View on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() must be(messages(title))
    }

    "display section header" in {

      createView().getElementById("section-header").text() must be("Your references")
    }

    "display empty input with label for Union code" in {

      val view = createView()

      view.getElementById("code-label").text() must be(messages(code))
      view.getElementById("code").attr("value") must be("")
    }

    "display empty input with label for Description" in {

      val view = createView()

      view.getElementById("description-label").text() must be(messages(description))
      view.getElementById("description").attr("value") must be("")
    }

    "display 'Back' button that links to 'Commodity measure' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be(s"/customs-declare-exports/declaration/items/$itemId/commodity-measure")
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

      view.getElementById("code").attr("value") must be("12345")
      view.getElementById("description").text() must be("12345")

    }

    "display data in code input" in {

      val view = createView(AdditionalInformation.form.fill(AdditionalInformation("12345", "")))

      view.getElementById("code").attr("value") must be("12345")
      view.getElementById("description").text() must be("")
    }

    "display data in description input" in {

      val view = createView(AdditionalInformation.form.fill(AdditionalInformation("", "12345")))

      view.getElementById("code").attr("value") must be("")
      view.getElementById("description").text() must be("12345")
    }

    "display one row with data in table" in {

      val view = additionalInformationPage(Mode.Normal, itemId, form, Seq(AdditionalInformation("12345", "12345")))

      view.select("table>tbody>tr>th:nth-child(1)").text() must be("12345-12345")

      val removeButton = view.select("table>tbody>tr>th:nth-child(2)>button")
      removeButton.text() must be(messages(removeCaption))
      removeButton.attr("name") must be(messages(removeCaption))
    }
  }
}
