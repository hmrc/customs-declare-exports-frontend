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
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.warehouse_identification
import views.tags.ViewTest

@ViewTest
class WarehouseIdentificationViewSpec extends ViewSpec with WarehouseIdentificationMessages with CommonMessages {

  private val form: Form[WarehouseIdentification] = WarehouseIdentification.form()
  private def createView(form: Form[WarehouseIdentification] = form): Html =
    warehouse_identification(appConfig, form)(fakeRequest, messages)

  "Warehouse Identification View" should {

    "have proper labels for messages" in {

      assertMessage(title, "Enter more detail about the warehouse")
      assertMessage(titleHint, "Locations")
      assertMessage(identificationType, "2/7 Warehouse type")
      assertMessage(identificationNumber, "2/7 Enter the warehouse identification number")
      assertMessage(identificationNumberHint, "For example, 1234567GB")
      assertMessage(supervisingCustomsOffice, "5/27 Where is the supervising customs office?")
      assertMessage(supervisingCustomsOfficeHint, "This is an 8 digit code")
      assertMessage(inlandTransportMode, "7/5 What was the inland mode of transport?")
      assertMessage(
        inlandTransportModeHint,
        "The transport that will move the goods from their inland supervision to the port of arrival"
      )
      assertMessage(sea, "Sea transport")
      assertMessage(rail, "Rail transport")
      assertMessage(road, "Road transport")
      assertMessage(air, "Air transport")
      assertMessage(postalOrMail, "Postal or Mail")
      assertMessage(fixedTransportInstallations, "Fixed transport installations")
      assertMessage(inlandWaterway, "Inland waterway transport")
      assertMessage(unknown, "Mode unknown, for example own propulsion")
    }

    "have proper error labels for messages" in {

      assertMessage(identificationNumberError, "Incorrect Identification Number")
      assertMessage(supervisingCustomsOfficeError, "Supervising customs office is incorrect")
      assertMessage(inlandTransportModeError, "Please, choose valid inland mode of transport")
    }
  }

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

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }
}
