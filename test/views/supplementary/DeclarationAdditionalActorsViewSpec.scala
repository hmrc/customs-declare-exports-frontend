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

import forms.supplementary.DeclarationAdditionalActors
import play.api.data.Form
import play.twirl.api.Html
import views.helpers.{Item, ViewSpec}
import views.html.supplementary.declaration_additional_actors
import views.tags.ViewTest

@ViewTest
class DeclarationAdditionalActorsViewSpec extends ViewSpec {

  private val form: Form[DeclarationAdditionalActors] = DeclarationAdditionalActors.form()

  private val prefix = s"${basePrefix}additionalActors."
  private val prefixParty = s"${basePrefix}partyType."

  private val title = Item(prefix, "title")
  private val eori = Item(basePrefix, "eori")
  private val party = Item(basePrefix, "partyType")
  private val consolidator = Item(prefixParty, "CS")
  private val manufacturer = Item(prefixParty, "MF")
  private val freightForwarder = Item(prefixParty, "FW")
  private val warehouseKeeper = Item(prefixParty, "WH")

  private def createView(form: Form[DeclarationAdditionalActors] = form): Html = declaration_additional_actors(appConfig, form, Seq())(fakeRequest, messages)

  "Declaration Additional Actors View" should {

    "have proper messages for labels" in {

      assertMessage(title.withPrefix, "3/37 Add other party")
      assertMessage(party.withPrefix, "Party type")
      assertMessage(eori.withPrefix, "EORI number")
      assertMessage(eori.withHint, "Enter the EORI number or business details")
      assertMessage(consolidator.withPrefix, "Consolidator")
      assertMessage(manufacturer.withPrefix, "Manufacturer")
      assertMessage(freightForwarder.withPrefix, "Freight forwarder")
      assertMessage(warehouseKeeper.withPrefix, "Warehouse keeper")
    }

    "have proper messages for error labels" in {

      assertMessage(party.withError, "Party type is incorrect")
      assertMessage(eori.withError, "EORI number is incorrect")
    }
  }

  "Declaration Additional Actors View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title.withPrefix))
    }

    "display header" in {

      getElementByCss(createView(), "legend>h1").text() must be(messages(title.withPrefix))
    }

    "display empty input with label for EORI" in {

      val view = createView()

      // will grab the first element
      getElementByCss(view, "label.form-label>span").text() must be(messages(eori.withPrefix))
      getElementByCss(view, "label.form-label>span.form-hint").text() must be(messages(eori.withHint))
      getElementById(view, eori.key).attr("value") must be("")
    }

    "display four radio buttons with description (not selected)" in {

      val view = createView(DeclarationAdditionalActors.form().fill(DeclarationAdditionalActors(Some(""), Some(""))))

      val optionOne = getElementById(view, consolidator.withPrefix)
      optionOne.attr("checked") must be("")

      val optionOneLabel = getElementByCss(view, "#partyType>div:nth-child(2)>label")
      optionOneLabel.text() must be(messages(consolidator.withPrefix))

      val optionTwo = getElementById(view, manufacturer.withPrefix)
      optionTwo.attr("checked") must be("")

      val optionTwoLabel = getElementByCss(view, "#partyType>div:nth-child(3)>label")
      optionTwoLabel.text() must be(messages(manufacturer.withPrefix))

      val optionThree = getElementById(view, freightForwarder.withPrefix)
      optionThree.attr("checked") must be("")

      val optionThreeLabel = getElementByCss(view, "#partyType>div:nth-child(4)>label")
      optionThreeLabel.text() must be(messages(freightForwarder.withPrefix))

      val optionFour = getElementById(view, warehouseKeeper.withPrefix)
      optionFour.attr("checked") must be("")

      val optionFourLabel = getElementByCss(view, "#partyType>div:nth-child(5)>label")
      optionFourLabel.text() must be(messages(warehouseKeeper.withPrefix))
    }

    "display \"Back\" button that links to \"Representative Details\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be("Back")
      backButton.attr("href") must be("/customs-declare-exports/declaration/supplementary/representative-details")
    }

    "display \"Save and continue\" button on page" in {

      val view = createView()

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be("Save and continue")
    }
  }

  "Declaration Additional Actors View with invalid input" should {

    "display errors when EORI is provided, but is incorrect" in {

      val view = createView(DeclarationAdditionalActors.form()
        .withError("eori", messages(eori.withError))
        .withError(party.key, messages(party.withError)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, eori.withError, eori.asLink)
      checkErrorLink(view, 2, party.withError, party.asLink)

      getElementByCss(view, "#error-message-eori-input").text() must be(messages(eori.withError))
      getElementByCss(view, "#error-message-partyType-input").text() must be(messages(party.withError))
    }

    "display error when EORI is provided, but party is not selected" in {

      val view = createView(DeclarationAdditionalActors.form()
        .withError(party.key, messages(party.withError)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, party.withError, party.asLink)

      getElementByCss(view, "#error-message-partyType-input").text() must be(messages(party.withError))
    }
  }

  "Declaration Additional Actors View when filled" should {

    "display EORI with CS selected" in {

      val view = createView(DeclarationAdditionalActors.form().fill(DeclarationAdditionalActors(Some("1234"), Some("CS"))))

      getElementById(view, eori.key).attr("value") must be("1234")
      getElementById(view, consolidator.withPrefix).attr("checked") must be("checked")
    }

    "display EORI with MF selected" in {

      val view = createView(DeclarationAdditionalActors.form().fill(DeclarationAdditionalActors(Some("1234"), Some("MF"))))

      getElementById(view, eori.key).attr("value") must be("1234")
      getElementById(view, manufacturer.withPrefix).attr("checked") must be("checked")
    }

    "display EORI with FW selected" in {

      val view = createView(DeclarationAdditionalActors.form().fill(DeclarationAdditionalActors(Some("1234"), Some("FW"))))

      getElementById(view, eori.key).attr("value") must be("1234")
      getElementById(view, freightForwarder.withPrefix).attr("checked") must be("checked")
    }

    "display EORI with WH selected" in {

      val view = createView(DeclarationAdditionalActors.form().fill(DeclarationAdditionalActors(Some("1234"), Some("WH"))))

      getElementById(view, eori.key).attr("value") must be("1234")
      getElementById(view, warehouseKeeper.withPrefix).attr("checked") must be("checked")
    }
  }
}
