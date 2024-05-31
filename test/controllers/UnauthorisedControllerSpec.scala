/*
 * Copyright 2023 HM Revenue & Customs
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
import org.mockito.Mockito.{reset, verify, when}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.{unauthorised, unauthorisedAgent, unauthorisedEoriInTdr}

class UnauthorisedControllerSpec extends ControllerWithoutFormSpec {

  val unauthorisedPage = mock[unauthorised]
  val unauthorisedEoriInTdrPage = mock[unauthorisedEoriInTdr]
  val unauthorisedAgentPage = mock[unauthorisedAgent]

  val controller =
    new UnauthorisedController(mockTdrFeatureFlags, mcc, unauthorisedPage, unauthorisedEoriInTdrPage, unauthorisedAgentPage)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(mockTdrFeatureFlags.isTdrUnauthorisedMessageEnabled).thenReturn(false)
    when(unauthorisedPage(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(unauthorisedEoriInTdrPage()(any(), any())).thenReturn(HtmlFormat.empty)
    when(unauthorisedAgentPage(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    reset(mockTdrFeatureFlags, unauthorisedPage, unauthorisedEoriInTdrPage, unauthorisedAgentPage)
  }

  "Unauthorised controller" should {

    "return 200 (OK)" when {

      "onPageLoad method is invoked and" when {

        "user has insufficient enrolments" in {
          val result = controller.onPageLoad(UserIsNotEnrolled)(getRequest())
          status(result) must be(OK)

          verify(unauthorisedPage).apply(any())(any(), any())
        }

        "user has sufficient enrollments but the EORI is not in the allow list (TDR disabled)" in {
          val result = controller.onPageLoad(UserEoriNotAllowed)(getRequest())
          status(result) must be(OK)

          verify(unauthorisedPage).apply(any())(any(), any())
        }

        "user has sufficient enrollments but the EORI is not in the allow list (TDR enabled)" in {
          when(mockTdrFeatureFlags.isTdrUnauthorisedMessageEnabled).thenReturn(true)

          val result = controller.onPageLoad(UrlDirect)(getRequest())
          status(result) must be(OK)

          verify(unauthorisedEoriInTdrPage).apply()(any(), any())
        }

        "someone travels to the URL directly" in {
          val result = controller.onPageLoad(UrlDirect)(getRequest())
          status(result) must be(OK)

          verify(unauthorisedPage).apply(any())(any(), any())
        }
      }

      "onAgentKickOut method is invoked and" when {

        "User has agent affinity group" in {
          val result = controller.onAgentKickOut(UserIsAgent)(getRequest())
          status(result) must be(OK)

          verify(unauthorisedAgentPage).apply(any())(any(), any())
        }

        "someone travels to the URL directly" in {
          val result = controller.onAgentKickOut(UrlDirect)(getRequest())
          status(result) must be(OK)

          verify(unauthorisedAgentPage).apply(any())(any(), any())
        }
      }
    }
  }
}
