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

import forms.declaration.OfficeOfExit
import helpers.views.declaration.{CommonMessages, OfficeOfExitMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.html.declaration.office_of_exit
import views.declaration.spec.ViewSpec

class OfficeOfExitView extends ViewSpec with OfficeOfExitMessages with CommonMessages {

  private val form: Form[OfficeOfExit] = OfficeOfExit.form()
  private def createView(form: Form[OfficeOfExit] = form): Html = office_of_exit(appConfig, form)(fakeRequest, messages)

  "Office of Exit View" should {

    "have proper messages for labels" in {

      assertMessage(officeOfExit, "5/12 Where is the office of exit?")
      assertMessage(title, "Office of exit")
      assertMessage(hint, "This is an 8 digit code")
    }

    "have proper messages for error labels" in {

      assertMessage(officeOfExitEmpty, "Office of exit cannot be empty")
      assertMessage(officeOfExitError, "Office of exit is incorrect")
    }
  }

  "Office of Exit View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title))
    }

    "display header" in {

      getElementByCss(createView(), "legend>h1").text() must be(messages(title))
    }

    "display empty input with label for Country" in {

      val view = createView()

      getElementByCss(view, "form>div.form-field>label>span:nth-child(1)").text() must be(messages(officeOfExit))
      getElementByCss(view, "form>div.form-field>label>span.form-hint").text() must be(messages(hint))
      getElementById(view, "officeId").attr("value") must be("")
    }

    "display \"Back\" button that links to \"Location of Goods\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/location-of-goods")
    }

    "display \"Save and continue\" button" in {

      val saveButton = getElementByCss(createView(), "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Office of Exit View for invalid input" should {

    "display error when Office of Exit is incorrect" in {

      val view = createView(OfficeOfExit.form().fillAndValidate(OfficeOfExit("123456789")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, officeOfExitError, "#officeId")

      getElementByCss(view, "#error-message-officeId-input").text() must be(messages(officeOfExitError))
    }

    "display error when Office of Exit is empty" in {

      val view = createView(OfficeOfExit.form().fillAndValidate(OfficeOfExit("")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, officeOfExitEmpty, "#officeId")

      getElementByCss(view, "#error-message-officeId-input").text() must be(messages(officeOfExitEmpty))
    }
  }

  "Office of Exit View when filled" should {

    "display data in Office of Exit input" in {

      val view = createView(OfficeOfExit.form().fill(OfficeOfExit("12345678")))

      getElementById(view, "officeId").attr("value") must be("12345678")
    }
  }
}
