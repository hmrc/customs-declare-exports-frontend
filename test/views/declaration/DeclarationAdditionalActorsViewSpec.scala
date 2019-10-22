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
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.DeclarationAdditionalActors
import helpers.views.declaration.{CommonMessages, DeclarationAdditionalActorsMessages}
import models.{DeclarationType, Mode}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.declaration_additional_actors
import views.tags.ViewTest

@ViewTest
class DeclarationAdditionalActorsViewSpec
    extends UnitViewSpec with ExportsTestData with DeclarationAdditionalActorsMessages with CommonMessages with Stubs with Injector {

  private val form: Form[DeclarationAdditionalActors] = DeclarationAdditionalActors.form()
  private val declarationAdditionalActorsPage = new declaration_additional_actors(mainTemplate)
  private def createView(form: Form[DeclarationAdditionalActors] = form): Document =
    declarationAdditionalActorsPage(Mode.Normal, form, Seq())(journeyRequest(), messages)

  "Declaration Additional Actors" should {

    "have correct message keys" in {

      val messages = instanceOf[MessagesApi].preferred(journeyRequest())

      messages must haveTranslationFor("supplementary.additionalActors.title")
      messages must haveTranslationFor("supplementary.additionalActors.title.hint")
      messages must haveTranslationFor("supplementary.additionalActors.eori")
      messages must haveTranslationFor("supplementary.additionalActors.eori.isNotDefined")
      messages must haveTranslationFor("supplementary.additionalActors.partyType")
      messages must haveTranslationFor("supplementary.additionalActors.maximumAmount.error")
      messages must haveTranslationFor("supplementary.additionalActors.duplicated.error")
      messages must haveTranslationFor("supplementary.partyType")
      messages must haveTranslationFor("supplementary.partyType.CS")
      messages must haveTranslationFor("supplementary.partyType.MF")
      messages must haveTranslationFor("supplementary.partyType.FW")
      messages must haveTranslationFor("supplementary.partyType.WH")
      messages must haveTranslationFor("supplementary.partyType.empty")
      messages must haveTranslationFor("supplementary.partyType.error")
    }
  }

  "Declaration Additional Actors View on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() mustBe messages(title)
    }

    "display section header" in {

      createView().getElementById("section-header").text() must include(messages("supplementary.summary.parties.header"))
    }

    "display empty input with label for EORI" in {

      val view = createView()

      view.getElementById("eori-label").text() mustBe messages("supplementary.additionalActors.eori")
      view.getElementById("eori").attr("value") mustBe empty
    }

    "display four radio buttons with description (not selected)" in {

      val view = createView(DeclarationAdditionalActors.form().fill(DeclarationAdditionalActors(Some(""), Some(""))))

      val optionOne = view.getElementById(consolidator)
      optionOne.attr("checked") mustBe empty

      val optionOneLabel = view.getElementById("supplementary.partyType.CS-label")
      optionOneLabel.text() mustBe messages(consolidator)

      val optionTwo = view.getElementById(manufacturer)
      optionTwo.attr("checked") mustBe empty

      val optionTwoLabel = view.getElementById("supplementary.partyType.MF-label")
      optionTwoLabel.text() mustBe messages(manufacturer)

      val optionThree = view.getElementById(freightForwarder)
      optionThree.attr("checked") mustBe empty

      val optionThreeLabel = view.getElementById("supplementary.partyType.FW-label")
      optionThreeLabel.text() mustBe messages(freightForwarder)

      val optionFour = view.getElementById(warehouseKeeper)
      optionFour.attr("checked") mustBe empty

      val optionFourLabel = view.getElementById("supplementary.partyType.WH-label")
      optionFourLabel.text() mustBe messages(warehouseKeeper)
    }

    "display 'Back' button that links to 'Representative Details' page if on Supplementary journey" in {

      val view = declarationAdditionalActorsPage(Mode.Normal, form, Seq())(journeyRequest(DeclarationType.SUPPLEMENTARY), messages)

      val backButton = view.getElementById("link-back")

      backButton.text() mustBe messages(backCaption)
      backButton.attr("href") mustBe routes.RepresentativeDetailsController.displayPage().url
    }

    "display 'Back' button that links to 'Carrier Details' page if on Standard Journey" in {

      val view = declarationAdditionalActorsPage(Mode.Normal, form, Seq())(journeyRequest(DeclarationType.STANDARD), messages)
      val backButton = view.getElementById("link-back")

      backButton.text() mustBe messages(backCaption)
      backButton.attr("href") mustBe routes.CarrierDetailsController.displayPage().url
    }

    "display 'Back' button that links to 'Carrier Details' page if on Simplified Journey" in {

      val view = declarationAdditionalActorsPage(Mode.Normal, form, Seq())(journeyRequest(DeclarationType.SIMPLIFIED), messages)
      val backButton = view.getElementById("link-back")

      backButton.text() mustBe messages(backCaption)
      backButton.attr("href") mustBe routes.CarrierDetailsController.displayPage().url
    }

    "display both 'Add' and 'Save and continue' button on page" in {

      val view = createView()

      val addButton = view.getElementById("add")
      addButton.text() mustBe "site.add supplementary.additionalActors.add.hint"

      val saveAndContinueButton = view.getElementById("submit")
      saveAndContinueButton.text() mustBe messages(saveAndContinueCaption)

      val saveAndReturn = view.getElementById("submit_and_return")
      saveAndReturn.text() mustBe messages(saveAndReturnCaption)
      saveAndReturn.attr("name") mustBe SaveAndReturn.toString
    }
  }

  "Declaration Additional Actors View with invalid input" should {

    "display errors when EORI is provided, but is incorrect" in {

      val view = createView(
        DeclarationAdditionalActors
          .form()
          .fillAndValidate(DeclarationAdditionalActors(Some(TestHelper.createRandomAlphanumericString(18)), Some("")))
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("eori", "#eori")
      view must haveFieldErrorLink("partyType", "#partyType")

      view.select("#error-message-eori-input").text() mustBe messages(eoriError)
      view.select("#error-message-partyType-input").text() mustBe messages(partyTypeError)
    }

    "display error when EORI is provided, but party is not selected" in {

      val view = createView(
        DeclarationAdditionalActors
          .form()
          .fillAndValidate(DeclarationAdditionalActors(Some(TestHelper.createRandomAlphanumericString(17)), Some("")))
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("partyType", "#partyType")

      view.select("#error-message-partyType-input").text() mustBe messages(partyTypeError)
    }
  }

  "Declaration Additional Actors View when filled" should {

    "display EORI with CS selected" in {

      val view =
        createView(DeclarationAdditionalActors.form().fill(DeclarationAdditionalActors(Some("1234"), Some("CS"))))

      view.getElementById("eori").attr("value") mustBe "1234"
      view.getElementById(consolidator).attr("checked") mustBe "checked"
      view.getElementById(manufacturer).attr("checked") mustBe empty
      view.getElementById(freightForwarder).attr("checked") mustBe empty
      view.getElementById(warehouseKeeper).attr("checked") mustBe empty
    }

    "display EORI with MF selected" in {

      val view =
        createView(DeclarationAdditionalActors.form().fill(DeclarationAdditionalActors(Some("1234"), Some("MF"))))

      view.getElementById("eori").attr("value") mustBe "1234"
      view.getElementById(consolidator).attr("checked") mustBe empty
      view.getElementById(manufacturer).attr("checked") mustBe "checked"
      view.getElementById(freightForwarder).attr("checked") mustBe empty
      view.getElementById(warehouseKeeper).attr("checked") mustBe empty
    }

    "display EORI with FW selected" in {

      val view =
        createView(DeclarationAdditionalActors.form().fill(DeclarationAdditionalActors(Some("1234"), Some("FW"))))

      view.getElementById("eori").attr("value") mustBe "1234"
      view.getElementById(consolidator).attr("checked") mustBe empty
      view.getElementById(manufacturer).attr("checked") mustBe empty
      view.getElementById(freightForwarder).attr("checked") mustBe "checked"
      view.getElementById(warehouseKeeper).attr("checked") mustBe empty
    }

    "display EORI with WH selected" in {

      val view =
        createView(DeclarationAdditionalActors.form().fill(DeclarationAdditionalActors(Some("1234"), Some("WH"))))

      view.getElementById("eori").attr("value") mustBe "1234"
      view.getElementById(consolidator).attr("checked") mustBe empty
      view.getElementById(manufacturer).attr("checked") mustBe empty
      view.getElementById(freightForwarder).attr("checked") mustBe empty
      view.getElementById(warehouseKeeper).attr("checked") mustBe "checked"
    }

    "display one row with data in table" in {

      val view =
        declarationAdditionalActorsPage(Mode.Normal, form, Seq(DeclarationAdditionalActors(Some("12345"), Some("CS"))))(
          journeyRequest(),
          realMessagesApi.preferred(request)
        )

      view.select("table>thead>tr>th:nth-child(1)").text() mustBe "Party’s EORI number"
      view.select("table>thead>tr>th:nth-child(2)").text() mustBe "Party type"

      view.select("table>tbody>tr>td:nth-child(1)").text() mustBe "12345"
      view.select("table>tbody>tr>td:nth-child(2)").text() mustBe "Consolidator"

      val removeButton = view.select("table>tbody>tr>td:nth-child(3)>button")

      removeButton.text() must include("Remove Consolidator 12345")
      removeButton.attr("value") mustBe """{"eori":"12345","partyType":"CS"}"""
    }
  }
}
