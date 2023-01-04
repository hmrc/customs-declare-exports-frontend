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
import models.SignOutReason
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.{session_timed_out, user_signed_out}

class SignOutControllerSpec extends ControllerWithoutFormSpec with ScalaFutures {

  private val mcc = stubMessagesControllerComponents()
  private val sessionTimedOutPage = mock[session_timed_out]
  private val userSignedOutPage = mock[user_signed_out]

  private val controller = new SignOutController(mcc, sessionTimedOutPage, userSignedOutPage)

  override def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(sessionTimedOutPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)
    when(userSignedOutPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach(): Unit = {
    reset(sessionTimedOutPage)
    reset(userSignedOutPage)

    super.afterEach()
  }

  "SignOutController on signOut" when {

    "provided with SessionTimeout parameter" should {

      "return 303 (SEE_OTHER) status" in {
        val result = controller.signOut(SignOutReason.SessionTimeout)(getRequest())
        status(result) mustBe SEE_OTHER
      }

      "redirect to /we-signed-you-out" in {
        val result = controller.signOut(SignOutReason.SessionTimeout)(getRequest())
        redirectLocation(result) mustBe Some(controllers.routes.SignOutController.sessionTimeoutSignedOut.url)
      }
    }

    "provided with UserAction parameter" should {

      "return 303 (SEE_OTHER) status" in {
        val result = controller.signOut(SignOutReason.UserAction)(getRequest())
        status(result) mustBe SEE_OTHER
      }

      "redirect to /you-have-signed-out" in {
        val result = controller.signOut(SignOutReason.UserAction)(getRequest())
        redirectLocation(result) mustBe Some(controllers.routes.SignOutController.userSignedOut.url)
      }
    }
  }

  "SignOutController on sessionTimeoutSignedOut" should {

    val controller = new SignOutController(mcc, sessionTimedOutPage, userSignedOutPage)

    "call sessionTimedOutPage" in {
      controller.sessionTimeoutSignedOut()(getRequest()).futureValue
      verify(sessionTimedOutPage).apply()(any(), any())
    }

    "return 200 status" in {
      val result = controller.sessionTimeoutSignedOut()(getRequest())
      status(result) mustBe OK
    }
  }

  "SignOutController on userSignedOut" should {

    val controller = new SignOutController(mcc, sessionTimedOutPage, userSignedOutPage)

    "call userSignedOutPage" in {
      controller.userSignedOut()(getRequest()).futureValue
      verify(userSignedOutPage).apply()(any(), any())
    }

    "return 200 status" in {
      val result = controller.userSignedOut()(getRequest())
      status(result) mustBe OK
    }
  }
}
