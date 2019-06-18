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

import forms.declaration.AdditionalInformation
import helpers.views.declaration.{AdditionalInformationMessages, CommonMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.additional_information
import views.tags.ViewTest

@ViewTest
class AdditionalInformationViewSpec extends ViewSpec with AdditionalInformationMessages with CommonMessages {

  private val form: Form[AdditionalInformation] = AdditionalInformation.form()
  private def createView(form: Form[AdditionalInformation] = form): Html =
    additional_information(appConfig, form, Seq())(fakeRequest, messages)

  /*
   * Moved all errors tests to AdditionalInformationControllerSpec,
   * as the logic depends on which button we will press (we can't emulate it
   * at view tests)
   */
  "Additional Information View" should {

    "have proper messages for labels" in {

      assertMessage(additionalInformation, "Additional information")
      assertMessage(title, "2/2 Enter additional information")
      assertMessage(code, "Enter the union or national code")
      assertMessage(description, "Enter the information required")
    }

    "have proper messages for error labels" in {

      assertMessage(codeEmpty, "Code cannot be empty")
      assertMessage(codeError, "Code is incorrect")
      assertMessage(descriptionEmpty, "Information description cannot be empty")
      assertMessage(descriptionError, "Information description is incorrect")
    }
  }

  "Additional Information View on empty page" should {

    "display page title" in {

      getElementById(createView(), "title").text() must be(messages(title))
    }

    "display section header" in {

      getElementById(createView(), "section-header").text() must be("Your references")
    }

    "display empty input with label for Union code" in {

      val view = createView()

      getElementById(view, "code-label").text() must be(messages(code))
      getElementById(view, "code").attr("value") must be("")
    }

    "display empty input with label for Description" in {

      val view = createView()

      getElementById(view, "description-label").text() must be(messages(description))
      getElementById(view, "description").attr("value") must be("")
    }

    "display 'Back' button that links to 'Commodity measure' page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/commodity-measure")
    }

    "display both 'Add' and 'Save and continue' button on page" in {

      val view = createView()

      val addButton = getElementByCss(view, "#add")
      addButton.text() must be(messages(addCaption))

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Additional Information View when filled" should {

    "display data in both inputs" in {

      val view = createView(AdditionalInformation.form().fill(AdditionalInformation("12345", "12345")))

      getElementById(view, "code").attr("value") must be("12345")
      getElementById(view, "description").text() must be("12345")

    }

    "display data in code input" in {

      val view = createView(AdditionalInformation.form().fill(AdditionalInformation("12345", "")))

      getElementById(view, "code").attr("value") must be("12345")
      getElementById(view, "description").text() must be("")
    }

    "display data in description input" in {

      val view = createView(AdditionalInformation.form().fill(AdditionalInformation("", "12345")))

      getElementById(view, "code").attr("value") must be("")
      getElementById(view, "description").text() must be("12345")
    }

    "display one row with data in table" in {

      val view = additional_information(appConfig, form, Seq(AdditionalInformation("12345", "12345")))

      getElementByCss(view, "table>tbody>tr>th:nth-child(1)").text() must be("12345-12345")

      val removeButton = getElementByCss(view, "table>tbody>tr>th:nth-child(2)>button")
      removeButton.text() must be(messages(removeCaption))
      removeButton.attr("name") must be(messages(removeCaption))
    }
  }
}
