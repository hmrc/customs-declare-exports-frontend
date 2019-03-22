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
import forms.declaration.ProcedureCodes
import helpers.views.declaration.{CommonMessages, ProcedureCodesMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.html.declaration.procedure_codes
import views.declaration.spec.ViewSpec

class ProcedureCodesViewSpec extends ViewSpec with ProcedureCodesMessages with CommonMessages {

  private val form: Form[ProcedureCodes] = ProcedureCodes.form()
  private def createView(form: Form[ProcedureCodes] = form): Html =
    procedure_codes(appConfig, form, Seq())(fakeRequest, messages)

  /*
   * Tests for errors are in the ProcedureCodesPageControllerSpec
   */
  "Procedure Codes View" should {

    "have proper messages for labels" in {

      assertMessage(title, "Procedure Codes")
      assertMessage(procCodeHeader, "1/10 Enter the procedure code")
      assertMessage(procCodeHeaderHint, "A 4 digit code, made up of the requested procedure and the previous procedure")
      assertMessage(addProcCodeHeader, "1/11 Do you need to enter additional procedure codes?")
      assertMessage(addProcCodeHeaderHint, "A 3 digit code. Up to 99 may be declared for each goods item.")
    }

    "have proper messages for error labels" in {

      assertMessage(procCodeErrorEmpty, "Procedure code cannot be empty")
      assertMessage(procCodeErrorLength, "Procedure code must be exactly 4 characters long")
      assertMessage(procCodeErrorSpecialCharacters, "Procedure code cannot contain special characters")
      assertMessage(addProcCodeErrorLength, "Additional procedure code must be exactly 3 characters long")
      assertMessage(addProcCodeErrorSpecialCharacters, "Additional procedure code cannot contain special characters")
      assertMessage(addProcCodeErrorMandatory, "You must have at least one additional procedure code")
      assertMessage(addProcCodeErrorMaxAmount, "You can have up to 99 codes")
      assertMessage(addProcCodeErrorEmpty, "You cannot add empty code")
      assertMessage(addProcCodeErrorDuplication, "You cannot add the same code like before")
    }
  }

  "Procedure Codes View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title))
    }

    "display header" in {

      getElementByCss(createView(), "legend>h1").text() must be(messages(title))
    }

    "display empty input with label for Procedure Code" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(3)>label>span:nth-child(1)").text() must be(messages(procCodeHeader))
      getElementByCss(view, "form>div:nth-child(3)>label>span.form-hint").text() must be(messages(procCodeHeaderHint))
      getElementById(view, "procedureCode").attr("value") must be("")
    }

    "display empty input with label for Additional Procedure Codes" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(4)>label>span:nth-child(1)").text() must be(messages(addProcCodeHeader))
      getElementByCss(view, "form>div:nth-child(4)>label>span.form-hint").text() must be(
        messages(addProcCodeHeaderHint)
      )
      getElementById(view, "additionalProcedureCode").attr("value") must be("")
    }

    "display \"Back\" button that links to \"Export Items\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/export-items")
    }

    "display both \"Add\" and \"Save and continue\" button on page" in {

      val view = createView()

      val addButton = getElementByCss(view, "#add")
      addButton.text() must be(messages(addCaption))

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Procedure Codes View when filled" should {

    "display data in Procedure Code input" in {

      val view = createView(ProcedureCodes.form().fill(ProcedureCodes(Some("Test"), Some(""))))

      getElementById(view, "procedureCode").attr("value") must be("Test")
    }

    "display data in Additional Procedure Code input" in {

      val view = createView(ProcedureCodes.form().fill(ProcedureCodes(Some(""), Some("Test"))))

      getElementById(view, "additionalProcedureCode").attr("value") must be("Test")
    }

    "display data in both inputs" in {

      val view = createView(ProcedureCodes.form().fill(ProcedureCodes(Some("Test"), Some("Test"))))

      getElementById(view, "procedureCode").attr("value") must be("Test")
      getElementById(view, "additionalProcedureCode").attr("value") must be("Test")
    }
  }
}
