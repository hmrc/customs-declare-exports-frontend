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
import forms.supplementary.GoodsItemNumber
import helpers.{CommonMessages, GoodItemNumberMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.html.supplementary.good_item_number
import views.supplementary.spec.ViewSpec

class GoodItemNumberViewSpec extends ViewSpec with GoodItemNumberMessages with CommonMessages {

  private val form: Form[GoodsItemNumber] = GoodsItemNumber.form()
  private def createView(form: Form[GoodsItemNumber] = form): Html = good_item_number(appConfig, form)(fakeRequest, messages)

  "Good Item Number View" should {

    "have proper messages for labels" in {

      assertMessage(title, "Good item number")
      assertMessage(hint, "The number assigned to the item in the declaration")
      assertMessage(goodItem, "1/6 Enter the item number")
    }

    "have proper messages for error labels" in {

      assertMessage(goodItemError, "Incorrect item number")
    }
  }

  "Good Item Number View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title))
    }

    "display header" in {

      val view = createView()

      getElementByCss(view, "legend>h1").text() must be(messages(title))
    }

    "display empty input with label for Good Item number" in {

      val view = createView()

      getElementByCss(view, "form>div.form-field>label>span").text() must be(messages(goodItem))
      getElementByCss(view, "form>div.form-field>label>span.form-hint").text() must be(messages(hint))
      getElementById(view, "goodItemNumber").attr("value") must be("")
    }

    "display \"Back\" button that links to \"Previous Documents\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/supplementary/previous-documents")
    }

    "display \"Save and continue\" button" in {

      val saveButton = getElementByCss(createView(), "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Good Item Number View on invalid input" should {

    "display error when number is incorrect " in {

      val view = createView(GoodsItemNumber.form().fillAndValidate(GoodsItemNumber("000")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, goodItemError, "#goodItemNumber")

      getElementByCss(view, "#error-message-goodItemNumber-input").text() must be(messages(goodItemError))
    }
  }

  "Good Item Number View when filled" should {

    "display entered number" in {

      val form = GoodsItemNumber.form().fill(GoodsItemNumber("100"))
      val view = createView(form)

      getElementById(view, "goodItemNumber").attr("value") must be("100")
    }
  }
}
