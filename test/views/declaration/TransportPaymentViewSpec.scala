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

import forms.declaration.TransportPayment
import helpers.views.declaration.CommonMessages
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.transport_payment
import views.tags.ViewTest

@ViewTest
class TransportPaymentViewSpec extends UnitViewSpec with ExportsTestData with Stubs with CommonMessages {

  private val page = new transport_payment(mainTemplate)
  private val form: Form[TransportPayment] = TransportPayment.form()
  private val realMessages = validatedMessages
  private def createView: Document =
    page(Mode.Normal, form)(journeyRequest(), realMessages)

  "Transport Payment View" should {
    val view = createView

    "display page title" in {
      view.getElementById("title").text() must be(realMessages("declaration.transportData.transportPayment.paymentMethod"))
    }

    "display 'Back' button that links to 'border transport' page" in {
      val backLinkContainer = view.getElementById("link-back")

      backLinkContainer must containText(realMessages(backCaption))
      backLinkContainer.getElementById("link-back") must haveHref(controllers.declaration.routes.BorderTransportController.displayPage(Mode.Normal))
    }

    "display choices for payment method" in {
      val choices = view.getElementById("paymentMethod")
      choices must containText(realMessages("declaration.transportData.transportPayment.paymentMethod.cash"))
      choices must containText(realMessages("declaration.transportData.transportPayment.paymentMethod.creditCard"))
      choices must containText(realMessages("declaration.transportData.transportPayment.paymentMethod.cheque"))
      choices must containText(realMessages("declaration.transportData.transportPayment.paymentMethod.other"))
      choices must containText(realMessages("declaration.transportData.transportPayment.paymentMethod.eFunds"))
      choices must containText(realMessages("declaration.transportData.transportPayment.paymentMethod.accHolder"))
      choices must containText(realMessages("declaration.transportData.transportPayment.paymentMethod.notPrePaid"))
    }

    "display 'Save and continue' button on page" in {
      val saveButton = view.getElementById("submit")
      saveButton must containText(realMessages(saveAndContinueCaption))
    }

    "display 'Save and return' button on page" in {
      val saveAndReturnButton = view.getElementById("submit_and_return")
      saveAndReturnButton must containText(realMessages(saveAndReturnCaption))
    }
  }
}
