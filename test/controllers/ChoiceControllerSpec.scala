/*
 * Copyright 2024 HM Revenue & Customs
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

import base.ControllerWithoutFormSpec
import models.requests.SessionHelper.{declarationUuid, errorFixModeSessionKey, errorKey}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import play.api.http.Status.OK
import play.api.test.Helpers.await
import play.twirl.api.HtmlFormat
import views.html.choice_page

class ChoiceControllerSpec extends ControllerWithoutFormSpec {

  private val choicePage = mock[choice_page]

  private val controller = new ChoiceController(mockAuthAction, mockVerifiedEmailAction, mcc, choicePage)

  "ChoiceController.displayPage" should {

    "return 200 (OK)" in {
      authorizedUser()
      when(choicePage()(any(), any(), any())).thenReturn(HtmlFormat.empty)

      val result = await(controller.displayPage(getRequestWithSession(errorFixModeSessionKey -> "err1", errorKey -> "err2")))
      result.header.status mustBe OK

      List(declarationUuid, errorFixModeSessionKey, errorKey).foreach(result.header.headers.get(_) mustBe None)

      verify(choicePage)()(any(), any(), any())
    }
  }
}
