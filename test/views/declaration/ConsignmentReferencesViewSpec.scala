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

import base.{Injector, TestHelper}
import controllers.declaration.routes
import controllers.util.SaveAndReturn
import forms.declaration.ConsignmentReferences
import forms.{Ducr, Lrn}
import helpers.views.declaration.{CommonMessages, ConsignmentReferencesMessages}
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.consignment_references
import views.tags.ViewTest

@ViewTest
class ConsignmentReferencesViewSpec
    extends UnitViewSpec with ConsignmentReferencesMessages with CommonMessages with Stubs with Injector {

  private val properDUCR = "7GB000000000000-12345"
  private val incorrectDUCR = "7GB000000000000-1234512345123451234512345"

  private val form: Form[ConsignmentReferences] = ConsignmentReferences.form()
  private val consignmentReferencesPage = new consignment_references(mainTemplate)
  private def createView(form: Form[ConsignmentReferences] = form): Document =
    consignmentReferencesPage(Mode.Normal, form)(request, messages)

  "Consignment References" should {

    "have correct message keys" in {

      val messages = instanceOf[MessagesApi].preferred(request)

      messages must haveTranslationFor("supplementary.consignmentReferences.heading")
      messages must haveTranslationFor("supplementary.consignmentReferences.header")
      messages must haveTranslationFor("supplementary.consignmentReferences.ducr.info")
      messages must haveTranslationFor("supplementary.consignmentReferences.ducr.hint")
      messages must haveTranslationFor("supplementary.consignmentReferences.lrn.info")
      messages must haveTranslationFor("supplementary.consignmentReferences.lrn.hint")
      messages must haveTranslationFor("supplementary.consignmentReferences.lrn.error.empty")
      messages must haveTranslationFor("supplementary.consignmentReferences.lrn.error.length")
      messages must haveTranslationFor("supplementary.consignmentReferences.lrn.error.specialCharacter")
    }
  }

  "Consignment References View on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() mustBe messages(header)
    }

    "display section header" in {

      createView().getElementById("section-header").text() mustBe messages(
        "supplementary.consignmentReferences.heading"
      )
    }

    "display empty input with label for DUCR" in {

      val view = createView()

      view.getElementById("ducr_ducr-label").text() mustBe messages(ducrInfo)
      view.getElementById("ducr_ducr-hint").text() mustBe messages(ducrHint)
      view.getElementById("ducr_ducr").attr("value") mustBe empty
    }

    "display empty input with label for LRN" in {

      val view = createView()

      view.getElementById("lrn-label").text() mustBe messages(lrnInfo)
      view.getElementById("lrn-hint").text() mustBe messages(lrnHint)
      view.getElementById("lrn").attr("value") mustBe empty
    }

    "display 'Back' button that links to 'Declaration Type' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() mustBe messages(backCaption)
      backButton.attr("href") mustBe routes.AdditionalDeclarationTypeController.displayPage().url
    }

    "display 'Save and continue' button on page" in {
      val view = createView()
      val saveButton = view.getElementById("submit")
      saveButton.text() mustBe messages(saveAndContinueCaption)
    }

    "display 'Save and return' button on page" in {
      val view = createView()
      val saveButton = view.getElementById("submit_and_return")
      saveButton.text() mustBe messages(saveAndReturnCaption)
      saveButton.attr("name") mustBe SaveAndReturn.toString
    }
  }

  "Consignment References View for invalid input" should {

    "display error for empty LRN" in {

      val view =
        createView(ConsignmentReferences.form().fillAndValidate(ConsignmentReferences(Ducr(properDUCR), Lrn(""))))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("lrn", "#lrn")

      view.select("#error-message-lrn-input").text() mustBe messages(lrnEmpty)
    }

    "display error when LRN is longer then 22 characters" in {

      val view = createView(
        ConsignmentReferences
          .form()
          .fillAndValidate(ConsignmentReferences(Ducr(properDUCR), Lrn(TestHelper.createRandomAlphanumericString(23))))
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("lrn", "#lrn")

      view.select("#error-message-lrn-input").text() mustBe messages(lrnLength)
    }

    "display error when LRN contains special character" in {

      val view =
        createView(ConsignmentReferences.form().fillAndValidate(ConsignmentReferences(Ducr(properDUCR), Lrn("#@#$"))))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("lrn", "#lrn")

      view.select("#error-message-lrn-input").text() mustBe messages(lrnSpecialCharacter)
    }

    "display error when DUCR is incorrect and LRN empty" in {

      val view =
        createView(ConsignmentReferences.form().fillAndValidate(ConsignmentReferences(Ducr(incorrectDUCR), Lrn(""))))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("ducr.ducr", "#ducr_ducr")
      view must haveFieldErrorLink("lrn", "#lrn")

      view.select("#error-message-ducr_ducr-input").text() mustBe messages(ducrError)
      view.select("#error-message-lrn-input").text() mustBe messages(lrnEmpty)
    }

    "display error when DUCR is incorrect and LRN is longer then 22 characters" in {

      val view = createView(
        ConsignmentReferences
          .form()
          .fillAndValidate(
            ConsignmentReferences(Ducr(incorrectDUCR), Lrn(TestHelper.createRandomAlphanumericString(23)))
          )
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("ducr.ducr", "#ducr_ducr")
      view must haveFieldErrorLink("lrn", "#lrn")

      view.select("#error-message-ducr_ducr-input").text() mustBe messages(ducrError)
      view.select("#error-message-lrn-input").text() mustBe messages(lrnLength)
    }

    "display error when DUCR is incorrect and LRN contains special character" in {

      val view =
        createView(ConsignmentReferences.form().fillAndValidate(ConsignmentReferences(Ducr(incorrectDUCR), Lrn("$$%"))))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("ducr.ducr", "#ducr_ducr")
      view must haveFieldErrorLink("lrn", "#lrn")

      view.select("#error-message-ducr_ducr-input").text() mustBe messages(ducrError)
      view.select("#error-message-lrn-input").text() mustBe messages(lrnSpecialCharacter)
    }
  }

  "Consignment References View when filled" should {

    "display data in DUCR input" in {

      val view =
        createView(
          ConsignmentReferences.form().fill(ConsignmentReferences(Ducr("9GB12345678901234-SHIP1234-1"), Lrn("")))
        )

      view.getElementById("ducr_ducr").attr("value") mustBe "9GB12345678901234-SHIP1234-1"
      view.getElementById("lrn").attr("value") mustBe empty
    }

    "display data in LRN input" in {

      val view = createView(ConsignmentReferences.form().fill(ConsignmentReferences(Ducr(""), Lrn("test1"))))

      view.getElementById("ducr_ducr").attr("value") mustBe empty
      view.getElementById("lrn").attr("value") mustBe "test1"
    }

    "display data in all inputs" in {

      val view =
        createView(
          ConsignmentReferences.form().fill(ConsignmentReferences(Ducr("GB/ABC4-ASIUDYFAHSDJF"), Lrn("test1")))
        )

      view.getElementById("ducr_ducr").attr("value") mustBe "GB/ABC4-ASIUDYFAHSDJF"
      view.getElementById("lrn").attr("value") mustBe "test1"
    }

  }
}
