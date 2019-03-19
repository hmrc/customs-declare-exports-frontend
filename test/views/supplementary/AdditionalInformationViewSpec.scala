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

package views.supplementary

import forms.supplementary.AdditionalInformation
import play.api.data.Form
import play.twirl.api.Html
import views.helpers.{Item, ViewSpec}
import views.html.supplementary.additional_information
import views.tags.ViewTest

@ViewTest
class AdditionalInformationViewSpec extends ViewSpec {

  private val form :Form[AdditionalInformation] = AdditionalInformation.form()

  private val prefix = s"${basePrefix}additionalInformation."

  private val pageTitle = Item("supplementary.", "additionalInformation")
  private val title = Item(prefix, "title")
  private val code = Item(prefix, "code")
  private val description = Item(prefix, "description")
  private val limit = Item(basePrefix, "limit")
  private val duplication = Item(basePrefix, "duplication")
  private val oneItem = Item(basePrefix + "continue.", "mandatory")

  private def createView(form :Form[AdditionalInformation] = form) :Html = additional_information(appConfig, form, Seq())(fakeRequest, messages)

  "Additional Information View" should {

    "have proper messages for labels" in {

      assertMessage(pageTitle.withPrefix, "Additional information")
      assertMessage(title.withPrefix, "2/2 Additional information")
      assertMessage(code.withPrefix, "Enter the union or national code")
      assertMessage(description.withPrefix, "Enter the information required")
    }

    "have proper messages for error labels" in {

      assertMessage(code.withEmpty, "Code cannot be empty")
      assertMessage(code.withError, "Code is incorrect")
      assertMessage(description.withEmpty, "Information description cannot be empty")
      assertMessage(description.withError, "Information description is incorrect")
      assertMessage(limit.withPrefix, "You cannot add more items")
      assertMessage(duplication.withPrefix, "You cannot add duplicated value")
      assertMessage(oneItem.withPrefix, "You must add at least one item")
    }
  }

  "Additional Information View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(pageTitle.withPrefix))
    }

    "display header" in {

      getElementByCss(createView(), "legend>h1").text() must be(messages(title.withPrefix))
    }

    "display empty input with label for Union code" in {

      val view  = createView()

      getElementByCss(view, "form>div:nth-child(3)>label>span").text() must be(messages(code.withPrefix))
      getElementById(view, code.key).attr("value") must be("")
    }

    "display empty input with label for Description" in {

      val view  = createView()

      getElementByCss(view, "form>div:nth-child(4)>label>span").text() must be(messages(description.withPrefix))
      getElementById(view, description.key).attr("value") must be("")
    }

    "display \"Back\" button that links to \"Commodity measure \" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be("Back")
      backButton.attr("href") must be("/customs-declare-exports/declaration/supplementary/commodity-measure")
    }

    "display both \"Add\" and \"Save and continue\" button on page" in {

      val view = createView()

      val addButton = getElementByCss(view, "#add")
      addButton.text() must be("Add")

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be("Save and continue")
    }
  }

  "Additional Information View for invalid input" when {

    "adding" should {

      "display error for empty code" in {

        val view = createView(AdditionalInformation.form().withError(code.key, messages(code.withEmpty)))

        checkErrorsSummary(view)
        checkErrorLink(view, 1, code.withEmpty, code.asLink)

        getElementByCss(view, "#error-message-code-input").text() must be(messages(code.withEmpty))
      }

      "display error for empty description" in {

        val view = createView(AdditionalInformation.form().withError(description.key, messages(description.withEmpty)))

        checkErrorsSummary(view)
        checkErrorLink(view, 1, description.withEmpty, description.asLink)

        getElementByCss(view, "#error-message-description-input").text() must be(messages(description.withEmpty))
      }

      "display error for both inputs empty" in {

        val view = createView(AdditionalInformation.form()
          .withError(code.key, messages(code.withEmpty))
          .withError(description.key, messages(description.withEmpty))
        )

        checkErrorsSummary(view)
        checkErrorLink(view, 1, code.withEmpty, code.asLink)
        checkErrorLink(view, 2, description.withEmpty, description.asLink)

        getElementByCss(view, "#error-message-code-input").text() must be(messages(code.withEmpty))
        getElementByCss(view, "#error-message-description-input").text() must be(messages(description.withEmpty))
      }

      "display error for incorrect code" in {

        val view = createView(AdditionalInformation.form().withError(code.key, messages(code.withError)))

        checkErrorsSummary(view)
        checkErrorLink(view, 1, code.withError, code.asLink)

        getElementByCss(view, "#error-message-code-input").text() must be(messages(code.withError))
      }

      "display error for incorrect description" in {

        val view = createView(AdditionalInformation.form().withError(description.key, messages(description.withError)))

        checkErrorsSummary(view)
        checkErrorLink(view, 1, description.withError, description.asLink)

        getElementByCss(view, "#error-message-description-input").text() must be(messages(description.withError))
      }

      "display error for both inputs incorrect" in {

        val view = createView(AdditionalInformation.form()
          .withError(code.key, messages(code.withError))
          .withError(description.key, messages(description.withError))
        )

        checkErrorsSummary(view)
        checkErrorLink(view, 1, code.withError, code.asLink)
        checkErrorLink(view, 2, description.withError, description.asLink)

        getElementByCss(view, "#error-message-code-input").text() must be(messages(code.withError))
        getElementByCss(view, "#error-message-description-input").text() must be(messages(description.withError))
      }

      "display error for duplicated document" in {

        val view = createView(AdditionalInformation.form().withError("", messages(duplication.withPrefix)))

        checkErrorsSummary(view)
        checkErrorLink(view, 1, duplication.withPrefix, "#")
      }

      "display error for adding more documents then limit" in {

        val view = createView(AdditionalInformation.form().withError("", messages(limit.withError)))

        checkErrorsSummary(view)
        checkErrorLink(view, 1, limit.withError, "#")
      }
    }

    "saving" should {

      "display error for trying to save with invalid code" in {

        val view = createView(AdditionalInformation.form().withError(code.key, messages(code.withError)))

        checkErrorsSummary(view)
        checkErrorLink(view, 1, code.withError, code.asLink)

        getElementByCss(view, "#error-message-code-input").text() must be(messages(code.withError))
      }

      "display error for trying to save without any item" in {

        val view = createView(AdditionalInformation.form().withError("", messages(oneItem.withPrefix)))

        checkErrorsSummary(view)
        checkErrorLink(view, 1, oneItem.withPrefix, "#")
      }
    }
  }

  "Additional Information View when filled" should {

    "display data in both inputs" in {

      val view = createView(AdditionalInformation.form().fill(AdditionalInformation("12345", "12345")))

      getElementById(view, code.key).attr("value") must be("12345")
      getElementById(view, description.key).text() must be("12345")

    }

    "display data in code input" in {

      val view = createView(AdditionalInformation.form().fill(AdditionalInformation("12345", "")))

      getElementById(view, code.key).attr("value") must be("12345")
      getElementById(view, description.key).text() must be("")
    }

    "display data in description input" in {

      val view = createView(AdditionalInformation.form().fill(AdditionalInformation("", "12345")))

      getElementById(view, code.key).attr("value") must be("")
      getElementById(view, description.key).text() must be("12345")
    }

    "display one item in table" in {

      val view = additional_information(appConfig, form, Seq(AdditionalInformation("12345", "12345")))

      getElementByCss(view, "table>tbody>tr>th:nth-child(1)").text() must be("12345-12345")

      val removeButton = getElementByCss(view, "table>tbody>tr>th:nth-child(2)>button")
      removeButton.text() must be("Remove")
      removeButton.attr("name") must be("Remove")
    }
  }
}
