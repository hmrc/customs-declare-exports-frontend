/*
 * Copyright 2021 HM Revenue & Customs
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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.guidance.error_explanation
import views.html.guidance.send_by_roro

class GuidanceControllerSpec extends ControllerWithoutFormSpec {

  val errorExplanationPage = mock[error_explanation]
  val sendByRoroPage = mock[send_by_roro]

  val controller = new GuidanceController(stubMessagesControllerComponents(), errorExplanationPage, sendByRoroPage)

  "GuidanceController" should {

    "return 200 (OK)" when {

      "the errorExplanation method is invoked" in {
        when(errorExplanationPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)

        val result = controller.errorExplanation()(getRequest())
        status(result) must be(OK)
      }

      "the sendByRoroPage method is invoked" in {
        when(sendByRoroPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)

        val result = controller.sendByRoro()(getRequest())
        status(result) must be(OK)
      }
    }
  }
}
