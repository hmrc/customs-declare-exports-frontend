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

import forms.declaration.officeOfExit.{OfficeOfExitForms, OfficeOfExitSupplementary}
import helpers.views.declaration.{CommonMessages, OfficeOfExitMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.office_of_exit_supplementary
import views.tags.ViewTest

@ViewTest
class OfficeOfExitSupplementaryViewSpec extends ViewSpec with OfficeOfExitMessages with CommonMessages {

  private val form: Form[OfficeOfExitSupplementary] = OfficeOfExitForms.supplementaryForm()
  private val officeOfExitSupplementary = app.injector.instanceOf[office_of_exit_supplementary]
  private def createView(form: Form[OfficeOfExitSupplementary] = form): Html =
    officeOfExitSupplementary(form)(fakeRequest, messages)

  "Office of Exit View on empty page" should {

    "display page title" in {

      getElementById(createView(), "title").text() must be(messages(officeOfExit))
    }

    "display section header" in {

      getElementById(createView(), "section-header").text() must be("Locations")
    }

    "display empty input with label for Country" in {

      val view = createView()

      getElementById(view, "officeId-hint").text() must be(messages(hint))
      getElementById(view, "officeId").attr("value") must be("")
    }

    "display 'Back' button that links to 'Location of Goods' page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/location-of-goods")
    }

    "display 'Save and continue' button" in {
      val saveButton = createView().getElementById("submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }

    "display 'Save and return' button on page" in {
      val saveAndReturnButton = createView().getElementById("submit_and_return")
      saveAndReturnButton.text() must be(messages(saveAndReturnCaption))
    }
  }

  "Office of Exit View for invalid input" should {

    "display error when Office of Exit is incorrect" in {

      val view = createView(OfficeOfExitForms.supplementaryForm.fillAndValidate(OfficeOfExitSupplementary("123456789")))

      checkErrorsSummary(view)
      checkErrorLink(view, "officeId-error", officeOfExitLength, "#officeId")

      getElementById(view, "error-message-officeId-input").text() must be(messages(officeOfExitLength))
    }

    "display error when Office of Exit is empty" in {

      val view = createView(OfficeOfExitForms.supplementaryForm.fillAndValidate(OfficeOfExitSupplementary("")))

      checkErrorsSummary(view)
      checkErrorLink(view, "officeId-error", officeOfExitEmpty, "#officeId")

      getElementById(view, "error-message-officeId-input").text() must be(messages(officeOfExitEmpty))
    }
  }

  "Office of Exit View when filled" should {

    "display data in Office of Exit input" in {

      val view = createView(OfficeOfExitForms.supplementaryForm.fill(OfficeOfExitSupplementary("12345678")))

      getElementById(view, "officeId").attr("value") must be("12345678")
    }
  }
}
