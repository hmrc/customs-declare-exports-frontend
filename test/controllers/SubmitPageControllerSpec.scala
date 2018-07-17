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
import utils.FakeNavigator
import connectors.FakeDataCacheConnector
import controllers.actions._
import play.api.test.Helpers._
import forms.DeclarationSummary
import models.NormalMode

class SubmitPageControllerSpec extends ControllerSpecBase {

  def onwardRoute = routes.IndexController.onPageLoad()

  val declarationSummary = new DeclarationSummary()

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyCacheMap) =
    new SubmitPageController(
      frontendAppConfig,
      messagesApi,
      FakeDataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      new SubmitDeclaration(wsClient, "")
    )

  val testAnswer = "answer"

  "SubmitPage Controller" must {
    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
    }
  }
}
