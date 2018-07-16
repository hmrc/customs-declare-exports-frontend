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

import api.declaration.SubmitDeclaration
import play.api.data.Form
import play.api.libs.json.JsString
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.FakeNavigator
import connectors.FakeDataCacheConnector
import controllers.actions._
import play.api.test.Helpers._
import forms.SubmitPageFormProvider
import identifiers.SubmitPageId
import models.NormalMode
import views.html.submitPage

// TODO Fix me
class SubmitPageControllerSpec extends ControllerSpecBase {

//  def onwardRoute = routes.IndexController.onPageLoad()
//
//  val formProvider = new SubmitPageFormProvider()
//  val form = formProvider()
//
//  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyCacheMap) =
//    new SubmitPageController(frontendAppConfig, messagesApi, FakeDataCacheConnector, new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
//      dataRetrievalAction, new DataRequiredActionImpl, formProvider, new SubmitDeclaration(wsClient, ""))
//
//  def viewAsString(form: Form[_] = form) = submitPage(frontendAppConfig, form, NormalMode)(fakeRequest, messages).toString
//
//  val testAnswer = "answer"
//
//  "SubmitPage Controller" must {
//
//    "return OK and the correct view for a GET" in {
//      val result = controller().onPageLoad(NormalMode)(fakeRequest)
//
//      status(result) mustBe OK
//      contentAsString(result) mustBe viewAsString()
//    }
//
//    "populate the view correctly on a GET when the question has previously been answered" in {
//      val validData = Map(SubmitPageId.toString -> JsString(testAnswer))
//      val getRelevantData = new FakeDataRetrievalAction(Some(CacheMap(cacheMapId, validData)))
//
//      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)
//
//      contentAsString(result) mustBe viewAsString(form.fill(testAnswer))
//    }
//
//    "return a Bad Request and errors when invalid data is submitted" in {
//      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", ""))
//      val boundForm = form.bind(Map("value" -> ""))
//
//      val result = controller().onSubmit(NormalMode)(postRequest)
//
//      status(result) mustBe BAD_REQUEST
//      contentAsString(result) mustBe viewAsString(boundForm)
//    }
//  }
}
