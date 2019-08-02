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

import base.TestHelper
import forms.Choice.AllowedChoiceValues.{StandardDec, SupplementaryDec}
import forms.declaration.DeclarationAdditionalActors
import helpers.views.declaration.{CommonMessages, DeclarationAdditionalActorsMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.declaration_additional_actors
import views.tags.ViewTest

@ViewTest
class DeclarationAdditionalActorsViewSpec
    extends ViewSpec with DeclarationAdditionalActorsMessages with CommonMessages {

  private val form: Form[DeclarationAdditionalActors] = DeclarationAdditionalActors.form()
  private val declarationAdditionalActorsPage = app.injector.instanceOf[declaration_additional_actors]
  private def createView(form: Form[DeclarationAdditionalActors] = form): Html =
    declarationAdditionalActorsPage(form, Seq())(fakeJourneyRequest(SupplementaryDec), messages)

  "Declaration Additional Actors View" should {

    "have proper messages for labels" in {

      assertMessage(title, "3/37 Add other parties (Optional)")
      assertMessage(actorsEori, "EORI number")
      assertMessage(actorsPartyType, "Party type")
    }

    "have proper messages for error labels" in {

      assertMessage(actorsEoriNotDefined, "Please enter a EORI number")
      assertMessage(maximumActorsError, "You cannot have more than 99 actors")
      assertMessage(duplicatedActorsError, "You cannot add the same actor")
    }
  }

  "Declaration Additional Actors View on empty page" should {

    "display page title" in {

      getElementById(createView(), "title").text() must be(messages(title))
    }

    "display section header" in {

      getElementById(createView(), "section-header").text() must be("Parties")
    }

    "display empty input with label for EORI" in {

      val view = createView()

      getElementById(view, "eori-label").text() must be(messages(eori))
      getElementById(view, "eori-hint").text() must be(messages(eoriHint))
      getElementById(view, "eori").attr("value") must be("")
    }

    "display four radio buttons with description (not selected)" in {

      val view = createView(DeclarationAdditionalActors.form().fill(DeclarationAdditionalActors(Some(""), Some(""))))

      val optionOne = getElementById(view, consolidator)
      optionOne.attr("checked") must be("")

      val optionOneLabel = getElementById(view, "supplementary.partyType.CS-label")
      optionOneLabel.text() must be(messages(consolidator))

      val optionTwo = getElementById(view, manufacturer)
      optionTwo.attr("checked") must be("")

      val optionTwoLabel = getElementById(view, "supplementary.partyType.MF-label")
      optionTwoLabel.text() must be(messages(manufacturer))

      val optionThree = getElementById(view, freightForwarder)
      optionThree.attr("checked") must be("")

      val optionThreeLabel = getElementById(view, "supplementary.partyType.FW-label")
      optionThreeLabel.text() must be(messages(freightForwarder))

      val optionFour = getElementById(view, warehouseKeeper)
      optionFour.attr("checked") must be("")

      val optionFourLabel = getElementById(view, "supplementary.partyType.WH-label")
      optionFourLabel.text() must be(messages(warehouseKeeper))
    }

    "display 'Back' button that links to 'Representative Details' page if on Supplementary journey" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/representative-details")
    }

    "display 'Back' button that links to 'Carrier Details' page if on Standard Journey" in {

      val view = declarationAdditionalActorsPage(form, Seq())(fakeJourneyRequest(StandardDec), messages)
      val backButton = getElementById(view, "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/carrier-details")
    }

    "display both 'Add' and 'Save and continue' button on page" in {

      val view = createView()

      val addButton = getElementByCss(view, "#add")
      addButton.text() must be(messages(addCaption))

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Declaration Additional Actors View with invalid input" should {

    "display errors when EORI is provided, but is incorrect" in {

      val view = createView(
        DeclarationAdditionalActors
          .form()
          .fillAndValidate(DeclarationAdditionalActors(Some(TestHelper.createRandomAlphanumericString(18)), Some("")))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, eoriError, "#eori")
      checkErrorLink(view, 2, partyTypeError, "#partyType")

      getElementByCss(view, "#error-message-eori-input").text() must be(messages(eoriError))
      getElementByCss(view, "#error-message-partyType-input").text() must be(messages(partyTypeError))
    }

    "display error when EORI is provided, but party is not selected" in {

      val view = createView(
        DeclarationAdditionalActors
          .form()
          .fillAndValidate(DeclarationAdditionalActors(Some(TestHelper.createRandomAlphanumericString(17)), Some("")))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, partyTypeError, "#partyType")

      getElementByCss(view, "#error-message-partyType-input").text() must be(messages(partyTypeError))
    }
  }

  "Declaration Additional Actors View when filled" should {

    "display EORI with CS selected" in {

      val view =
        createView(DeclarationAdditionalActors.form().fill(DeclarationAdditionalActors(Some("1234"), Some("CS"))))

      getElementById(view, "eori").attr("value") must be("1234")
      getElementById(view, consolidator).attr("checked") must be("checked")
      getElementById(view, manufacturer).attr("checked") must be("")
      getElementById(view, freightForwarder).attr("checked") must be("")
      getElementById(view, warehouseKeeper).attr("checked") must be("")
    }

    "display EORI with MF selected" in {

      val view =
        createView(DeclarationAdditionalActors.form().fill(DeclarationAdditionalActors(Some("1234"), Some("MF"))))

      getElementById(view, "eori").attr("value") must be("1234")
      getElementById(view, consolidator).attr("checked") must be("")
      getElementById(view, manufacturer).attr("checked") must be("checked")
      getElementById(view, freightForwarder).attr("checked") must be("")
      getElementById(view, warehouseKeeper).attr("checked") must be("")
    }

    "display EORI with FW selected" in {

      val view =
        createView(DeclarationAdditionalActors.form().fill(DeclarationAdditionalActors(Some("1234"), Some("FW"))))

      getElementById(view, "eori").attr("value") must be("1234")
      getElementById(view, consolidator).attr("checked") must be("")
      getElementById(view, manufacturer).attr("checked") must be("")
      getElementById(view, freightForwarder).attr("checked") must be("checked")
      getElementById(view, warehouseKeeper).attr("checked") must be("")
    }

    "display EORI with WH selected" in {

      val view =
        createView(DeclarationAdditionalActors.form().fill(DeclarationAdditionalActors(Some("1234"), Some("WH"))))

      getElementById(view, "eori").attr("value") must be("1234")
      getElementById(view, consolidator).attr("checked") must be("")
      getElementById(view, manufacturer).attr("checked") must be("")
      getElementById(view, freightForwarder).attr("checked") must be("")
      getElementById(view, warehouseKeeper).attr("checked") must be("checked")
    }

    "display one row with data in table" in {

      val view = declarationAdditionalActorsPage(form, Seq(DeclarationAdditionalActors(Some("12345"), Some("CS"))))(
        fakeJourneyRequest(StandardDec),
        messages
      )

      getElementByCss(view, "table>thead>tr>th:nth-child(1)").text() must be("EORI number")
      getElementByCss(view, "table>thead>tr>th:nth-child(2)").text() must be("Party type")

      getElementByCss(view, "table>tbody>tr>td:nth-child(1)").text() must be("12345")
      getElementByCss(view, "table>tbody>tr>td:nth-child(2)").text() must be("CS")

      val removeButton = getElementByCss(view, "table>tbody>tr>td:nth-child(3)>button")

      removeButton.text() must be(messages(removeCaption))
      removeButton.attr("name") must be(messages(removeCaption))
      removeButton.attr("value") must be("""{"eori":"12345","partyType":"CS"}""")
    }
  }
}
