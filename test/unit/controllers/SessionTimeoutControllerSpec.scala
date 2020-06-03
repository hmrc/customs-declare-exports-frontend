/*
 * Copyright 2020 HM Revenue & Customs
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

package unit.controllers

import controllers.SessionTimeoutController
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.{ControllerSpec, ControllerWithoutFormSpec}
import views.html.session_timed_out

class SessionTimeoutControllerSpec extends ControllerWithoutFormSpec {

  val mcc = stubMessagesControllerComponents()
  val startPage = mock[session_timed_out]

  val controller = new SessionTimeoutController(mockAuthAction, mcc, startPage)(ec)

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(startPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(startPage)
    super.afterEach()
  }

  "Start Controller" should {

    "return 200" when {

      "display signed out method is invoked" in {

        val result = controller.signedOut()(getRequest())

        status(result) must be(OK)
      }
    }

    "return 303" when {

      "display sign out method is invoked" in {
        authorizedUser()

        val result = controller.signOut()(getRequest())

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.routes.SessionTimeoutController.signedOut().url))
      }
    }
  }
}
