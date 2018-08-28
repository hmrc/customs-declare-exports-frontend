/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers

import connectors.FakeDataCacheConnector
import controllers.actions._
import forms.RepresentativesAddressFormProvider
import models.{Address, NormalMode}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.representativesAddress

class RepresentativesAddressControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = routes.DeclarationSummaryController.onPageLoad()

  val formProvider = new RepresentativesAddressFormProvider()
  val form = formProvider()

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyCacheMap) =
    new RepresentativesAddressController(
      appConfig,
      messagesApi,
      FakeDataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  def viewAsString(form: Form[_] = form): String =
    representativesAddress(appConfig, form, NormalMode)(fakeRequest, messages).toString

  val testAnswer = Address("Fullname", "Building", "Street", "town", None, "postcode", "country")

  "Representatives address controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("fullName" -> "Full name"),
        ("building" -> "Building"),
        ("street" -> "Street"),
        ("townOrCity" -> "Town"),
        ("county" -> "County"),
        ("postcode" -> "Postcode"),
        ("country" -> "Country")
      )

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }
  }
}
