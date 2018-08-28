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
import connectors.FakeDataCacheConnector
import controllers.actions._
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import utils.FakeNavigator

class DeclarationSummaryControllerSpec extends ControllerSpecBase with MockitoSugar {

  trait Scope {
    val mockFakeNavigator = mock[FakeNavigator]
    val mockSubmitDeclaration = mock[SubmitDeclaration]

    def controller(dataRetrievalAction: DataRetrievalAction = getEmptyCacheMap) =
      new DeclarationSummaryController(
        appConfig,
        messagesApi,
        FakeDataCacheConnector,
        mockFakeNavigator,
        FakeAuthAction,
        dataRetrievalAction,
        new DataRequiredActionImpl,
        mockSubmitDeclaration
      )
  }

  "Declaration summary controller" must {
    "return OK and the correct view for a GET" in new Scope {
      val result = controller().onPageLoad()(fakeRequest)

      status(result) mustBe OK
    }
  }
}
