/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.declaration.routes.ExpressConsignmentController
import forms.declaration.TransportPayment.form
import models.DeclarationType.STANDARD
import models.requests.JourneyRequest
import models.DeclarationType
import org.jsoup.nodes.Document
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.transport_payment
import views.tags.ViewTest

@ViewTest
class TransportPaymentViewSpec extends PageWithButtonsSpec with Injector {

  val page = instanceOf[transport_payment]

  override val typeAndViewInstance = (STANDARD, page(form)(_, _))

  def createView()(implicit request: JourneyRequest[_]): Document = page(form())(request, messages)

  "Transport Payment View" must {
    onJourney(DeclarationType.SIMPLIFIED, DeclarationType.STANDARD) { implicit request =>
      val view = createView()

      "display page title" in {
        view.getElementsByTag("h1") must containMessageForElements("declaration.transportInformation.transportPayment.paymentMethod")
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.6")
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
        choices must containMessage("declaration.transportInformation.transportPayment.paymentMethod.notAvailable")
      }

      checkAllSaveButtonsAreDisplayed(createView())

      "display 'Back' button that links to the 'Express Consignment' page" in {
        val backLinkContainer = view.getElementById("back-link")

        backLinkContainer must containMessage(backToPreviousQuestionCaption)
        backLinkContainer.getElementById("back-link") must haveHref(ExpressConsignmentController.displayPage())
      }
    }
  }
}
