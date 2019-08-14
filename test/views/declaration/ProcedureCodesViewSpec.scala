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
import views.tags.ViewTest

@ViewTest
class ProcedureCodesViewSpec extends ViewSpec with ProcedureCodesMessages with CommonMessages {

  private val form: Form[ProcedureCodes] = ProcedureCodes.form()
  private val procedureCodesPage = app.injector.instanceOf[procedure_codes]
  private def createView(form: Form[ProcedureCodes] = form): Html =
    procedureCodesPage("1234", form, Seq())(fakeRequest, messages)

  "Procedure Codes View on empty page" should {

    "display page title" in {

      getElementById(createView(), "title").text() must be(messages(title))
    }

    "display section header" in {

      getElementById(createView(), "section-header").text() must be("Items")
    }

    "display empty input with label for Procedure Code" in {

      val view = createView()

      getElementById(view, "procedureCode-label").text() must be(messages(procCodeHeader))
      getElementById(view, "procedureCode-hint").text() must be(messages(procCodeHeaderHint))
      getElementById(view, "procedureCode").attr("value") must be("")
    }

    "display empty input with label for Additional Procedure Codes" in {

      val view = createView()

      getElementById(view, "additionalProcedureCode-label").text() must be(messages(addProcCodeHeader))
      getElementById(view, "additionalProcedureCode-hint").text() must be(messages(addProcCodeHeaderHint))
      getElementById(view, "additionalProcedureCode").attr("value") must be("")
    }

    "display 'Back' button that links to 'Export Items' page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/export-items")
    }

    "display both 'Add' and 'Save and continue' button on page" in {

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
      getElementById(view, "additionalProcedureCode").attr("value") must be("")
    }

    "display data in Additional Procedure Code input" in {

      val view = createView(ProcedureCodes.form().fill(ProcedureCodes(Some(""), Some("Test"))))

      getElementById(view, "procedureCode").attr("value") must be("")
      getElementById(view, "additionalProcedureCode").attr("value") must be("Test")
    }

    "display data in both inputs" in {

      val view = createView(ProcedureCodes.form().fill(ProcedureCodes(Some("Test"), Some("Test"))))

      getElementById(view, "procedureCode").attr("value") must be("Test")
      getElementById(view, "additionalProcedureCode").attr("value") must be("Test")
    }
  }
}
