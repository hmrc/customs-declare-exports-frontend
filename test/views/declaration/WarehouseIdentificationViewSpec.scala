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

import forms.declaration.WarehouseIdentification
import helpers.views.declaration.{CommonMessages, WarehouseIdentificationMessages}
import models.Mode
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.warehouse_identification
import views.tags.ViewTest

@ViewTest
class WarehouseIdentificationViewSpec extends ViewSpec with WarehouseIdentificationMessages with CommonMessages {

  private val form: Form[WarehouseIdentification] = WarehouseIdentification.form()

  private val warehouseIdentificationPage = app.injector.instanceOf[warehouse_identification]

  private def createView(form: Form[WarehouseIdentification] = form): Html =
    warehouseIdentificationPage(Mode.Normal, form)(fakeRequest, messages)

  "Warehouse Identification View" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title))
    }

    "display header" in {

      getElementByCss(createView(), "legend>h1").text() must be(messages(title))
    }

    "display 'Back' button that links to 'Supervising Office' page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/export-items")
    }

    "display 'Save and continue' button on page" in {

      val view = createView()

      val saveButton = view.getElementById("submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }

    "display 'Save and return' button on page" in {

      val view = createView()

      val saveAndReturnButton = view.getElementById("submit_and_return")
      saveAndReturnButton.text() must be(messages(saveAndReturnCaption))
    }
  }
}
