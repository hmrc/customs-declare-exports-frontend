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

import forms.declaration.SupervisingCustomsOffice
import helpers.views.declaration.{CommonMessages, SupervisingCustomsOfficeMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.supervising_office
import views.tags.ViewTest

@ViewTest
class SupervisingCustomsOfficeViewSpec extends ViewSpec with SupervisingCustomsOfficeMessages with CommonMessages {

  private val form: Form[SupervisingCustomsOffice] = SupervisingCustomsOffice.form()
  private def createView(form: Form[SupervisingCustomsOffice] = form): Html =
    supervising_office(appConfig, form)(fakeRequest, messages)

  "Supervising Customs Office View" should {

    "have proper messages for labels" in {

      assertMessage(title, "5/27 Which supervising customs office was used?")
      assertMessage(supervisingCustomOffice, "5/27 Where is the supervising customs office?")
      assertMessage(hint, "This is an 8 digit code")
    }

    "have proper error messages for labels" in {

      assertMessage(scoEmpty, "Supervising customs office cannot be empty")
      assertMessage(scoError, "Supervising customs office is incorrect")
    }
  }

  "Supervising Customs Office View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title))
    }

    "display section header" in {

      getElementById(createView(), "section-header").text() must be("Locations")
    }

    "display header" in {

      getElementByCss(createView(), "legend>h1").text() must be(messages(title))
    }

    "display empty input with label for Supervising Customs Office" in {

      val view = createView()

      getElementByCss(view, "form>div.form-field>label>span.form-hint").text() must be(messages(hint))
      getElementById(view, "supervisingCustomsOffice").attr("value") must be("")
    }

    "display \"Back\" button that links to \"Previous Documents\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/previous-documents")
    }

    "display \"Save and continue\" button on page" in {

      val view = createView()

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Supervising Customs Office View for invalid input" should {

    "display error when Supervising Customs Office is incorrect" in {

      val view =
        createView(SupervisingCustomsOffice.form().fillAndValidate(SupervisingCustomsOffice(Some("123456789"))))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, scoError, "#supervisingCustomsOffice")

      getElementByCss(view, "#error-message-supervisingCustomsOffice-input").text() must be(messages(scoError))
    }
  }

  "Supervising Customs Office View when filled" should {

    "display data in Supervising Customs Office input" in {

      val view = createView(SupervisingCustomsOffice.form().fill(SupervisingCustomsOffice(Some("12345678"))))

      getElementById(view, "supervisingCustomsOffice").attr("value") must be("12345678")
    }
  }
}
