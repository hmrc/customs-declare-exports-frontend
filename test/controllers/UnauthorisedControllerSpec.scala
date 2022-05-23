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

package controllers

import base.ControllerWithoutFormSpec
import models.UnauthorisedReason.{UrlDirect, UserEoriNotAllowed, UserIsAgent, UserIsNotEnrolled}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.{unauthorised, unauthorisedAgent, unauthorisedEori}

class UnauthorisedControllerSpec extends ControllerWithoutFormSpec {

  val unauthorisedPage = mock[unauthorised]
  val unauthorisedEoriPage = mock[unauthorisedEori]
  val unauthorisedAgentPage = mock[unauthorisedAgent]

  val controller =
    new UnauthorisedController(
      mockTdrUnauthorisedMsgConfig,
      stubMessagesControllerComponents(),
      unauthorisedPage,
      unauthorisedEoriPage,
      unauthorisedAgentPage
    )

  "Unauthorised controller" should {

    "return 200 (OK)" when {

      "onPageLoad method is invoked and" when {

        "user has insufficient enrolments" in {
          when(unauthorisedPage(any())(any(), any())).thenReturn(HtmlFormat.empty)
          val result = controller.onPageLoad(UserIsNotEnrolled)(getRequest())
          status(result) must be(OK)
        }

        "user has sufficient enrollments but the EORI is not in the allow list" in {
          when(unauthorisedEoriPage()(any(), any())).thenReturn(HtmlFormat.empty)
          val result = controller.onPageLoad(UserEoriNotAllowed)(getRequest())
          status(result) must be(OK)
        }

        "tdr is enabled" in {
          when(mockTdrUnauthorisedMsgConfig.isTdrUnauthorisedMessageEnabled).thenReturn(true)
          when(unauthorisedEoriPage()(any(), any())).thenReturn(HtmlFormat.empty)
          val result = controller.onPageLoad(UrlDirect)(getRequest())
          status(result) must be(OK)
        }

        "someone travels to the URL directly" in {
          when(unauthorisedPage(any())(any(), any())).thenReturn(HtmlFormat.empty)
          val result = controller.onPageLoad(UrlDirect)(getRequest())
          status(result) must be(OK)
        }
      }

      "onAgentKickOut method is invoked and" when {

        "User has agent affinity group" in {
          when(unauthorisedAgentPage(any())(any(), any())).thenReturn(HtmlFormat.empty)
          val result = controller.onAgentKickOut(UserIsAgent)(getRequest())
          status(result) must be(OK)
        }

        "someone travels to the URL directly" in {
          when(unauthorisedAgentPage(any())(any(), any())).thenReturn(HtmlFormat.empty)
          val result = controller.onAgentKickOut(UrlDirect)(getRequest())
          status(result) must be(OK)
        }
      }
    }
  }
}
