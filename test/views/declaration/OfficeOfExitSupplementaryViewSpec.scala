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
import forms.declaration.officeOfExit.{OfficeOfExit, OfficeOfExitSupplementary}
import helpers.views.declaration.{CommonMessages, OfficeOfExitMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.html.declaration.office_of_exit_supplementary
import views.declaration.spec.ViewSpec
import views.tags.ViewTest

@ViewTest
class OfficeOfExitSupplementaryViewSpec extends ViewSpec with OfficeOfExitMessages with CommonMessages {

  private val form: Form[OfficeOfExitSupplementary] = OfficeOfExit.supplementaryForm()
  private def createView(form: Form[OfficeOfExitSupplementary] = form): Html =
    office_of_exit_supplementary(form)(fakeRequest, appConfig, messages)

  "Office of Exit View for supplementary" should {

    "have proper messages for labels" in {

      assertMessage(officeOfExit, "5/12 Where is the office of exit?")
      assertMessage(title, "Office of exit")
      assertMessage(hint, "This is an 8 digit code")
    }

    "have proper messages for error labels" in {

      assertMessage(officeOfExitEmpty, "Office of exit cannot be empty")
      assertMessage(officeOfExitLength, "The code must be 8 characters")
    }
  }

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

      val view = createView(OfficeOfExit.supplementaryForm.fillAndValidate(OfficeOfExitSupplementary("123456789")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, officeOfExitLength, "#officeId")

      getElementByCss(view, "#error-message-officeId-input").text() must be(messages(officeOfExitLength))
    }

    "display error when Office of Exit is empty" in {

      val view = createView(OfficeOfExit.supplementaryForm.fillAndValidate(OfficeOfExitSupplementary("")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, officeOfExitEmpty, "#officeId")

      getElementByCss(view, "#error-message-officeId-input").text() must be(messages(officeOfExitEmpty))
    }
  }

  "Office of Exit View when filled" should {

    "display data in Office of Exit input" in {

      val view = createView(OfficeOfExit.supplementaryForm.fill(OfficeOfExitSupplementary("12345678")))

      getElementById(view, "officeId").attr("value") must be("12345678")
    }
  }
}
