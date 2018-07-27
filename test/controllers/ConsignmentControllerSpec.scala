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
import forms.ConsignmentChoice.{Consolidation, SingleShipment}
import forms.{ConsignmentData, ConsignmentFormProvider}
import identifiers.ConsignmentId
import models.NormalMode
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.FakeNavigator
import views.html.consignment

class ConsignmentControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = routes.IndexController.onPageLoad()

  val formProvider = new ConsignmentFormProvider()
  val form = formProvider()

  val correctMucr = "A:GBP23"
  val correctDucr = "5GB123456789000-123ABC456DEFIIIIIII"

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyCacheMap) =
    new ConsignmentController(
      frontendAppConfig,
      messagesApi,
      FakeDataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      formProvider
    )

  def viewAsString(form: Form[_] = form): String =
    consignment(frontendAppConfig, form, NormalMode)(fakeRequest, messages).toString

  "Consignment Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered for consolidation" in {
      val data =
        JsObject(
          Map(
            "choice" -> JsString("consolidation"),
            "mucrConsolidation" -> JsString(correctMucr),
            "ducrConsolidation" -> JsString(correctDucr),
            "ducrSingleShipment" -> JsString("")
          )
        )

      val validData = Map(ConsignmentId.toString -> data)
      val expectResult = ConsignmentData(Consolidation, Some(correctMucr), Some(correctDucr), None)
      val getRelevantData = new FakeDataRetrievalAction(Some(CacheMap(cacheMapId, validData)))

      val result = controller(getRelevantData).onPageLoad()(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(expectResult))
    }

    "populate the view correctly on a GET when the question has previously been answered for single shipment" in {
      val data =
        JsObject(
          Map(
            "choice" -> JsString("singleShipment"),
            "mucrConsolidation" -> JsString(""),
            "ducrConsolidation" -> JsString(""),
            "ducrSingleShipment" -> JsString(correctDucr)
          )
        )

      val validData = Map(ConsignmentId.toString -> data)
      val expectResult = ConsignmentData(SingleShipment, None, None, Some(correctDucr))
      val getRelevantData = new FakeDataRetrievalAction(Some(CacheMap(cacheMapId, validData)))

      val result = controller(getRelevantData).onPageLoad()(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(expectResult))
    }

    "redirect to the next page when valid data is submitted for consolidation" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("choice" -> "consolidation"),
        ("mucrConsolidation" -> correctMucr),
        ("ducrConsolidation" -> correctDucr),
        ("ducrSingleShipment" -> "")
      )

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to the next page when valid data is submitted for single shipment" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("choice" -> "singleShipment"),
        ("mucrConsolidation" -> ""),
        ("ducrConsolidation" -> ""),
        ("ducrSingleShipment" -> correctDucr)
      )

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value" -> "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }
  }
}
