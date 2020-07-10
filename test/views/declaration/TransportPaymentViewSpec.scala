/*
 * Copyright 2020 HM Revenue & Customs
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

import base.Injector
import forms.declaration.TransportPayment
import helpers.views.declaration.CommonMessages
import models.requests.JourneyRequest
import models.{DeclarationType, Mode}
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec2
import views.html.declaration.transport_payment
import views.tags.ViewTest

@ViewTest
class TransportPaymentViewSpec extends UnitViewSpec2 with ExportsTestData with Stubs with CommonMessages with Injector {

  private val page = instanceOf[transport_payment]
  private val form: Form[TransportPayment] = TransportPayment.form()
  private def createView()(implicit request: JourneyRequest[_]): Document =
    page(Mode.Normal, form)(request, messages)

  "Transport Payment View" must {
    onJourney(DeclarationType.SIMPLIFIED, DeclarationType.STANDARD) { implicit request =>
      val view = createView()

      "display page title" in {
        view.getElementsByTag("h1") must containMessageForElements("declaration.transportInformation.transportPayment.paymentMethod")
      }

      "display choices for payment method" in {
        val choices = view.getElementsByClass("govuk-fieldset").first()
        choices must containMessage("declaration.transportInformation.transportPayment.paymentMethod.cash")
        choices must containMessage("declaration.transportInformation.transportPayment.paymentMethod.creditCard")
        choices must containMessage("declaration.transportInformation.transportPayment.paymentMethod.cheque")
        choices must containMessage("declaration.transportInformation.transportPayment.paymentMethod.other")
        choices must containMessage("declaration.transportInformation.transportPayment.paymentMethod.eFunds")
        choices must containMessage("declaration.transportInformation.transportPayment.paymentMethod.accHolder")
        choices must containMessage("declaration.transportInformation.transportPayment.paymentMethod.notPrePaid")
      }

      "display 'Save and continue' button on page" in {
        val saveButton = view.getElementById("submit")
        saveButton must containMessage(saveAndContinueCaption)
      }

      "display 'Save and return' button on page" in {
        val saveAndReturnButton = view.getElementById("submit_and_return")
        saveAndReturnButton must containMessage(saveAndReturnCaption)
      }
    }

    onStandard { implicit request =>
      val view = createView()

      "display 'Back' button that links to 'border transport' page" in {
        val backLinkContainer = view.getElementById("back-link")

        backLinkContainer must containMessage(backCaption)
        backLinkContainer.getElementById("back-link") must haveHref(controllers.declaration.routes.BorderTransportController.displayPage(Mode.Normal))
      }
    }

    onSimplified { implicit request =>
      val view = createView()

      "display 'Back' button that links to 'warehouse identification' page" in {
        val backLinkContainer = view.getElementById("back-link")

        backLinkContainer must containMessage(backCaption)
        backLinkContainer.getElementById("back-link") must haveHref(
          controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage(Mode.Normal)
        )
      }
    }
  }
}
