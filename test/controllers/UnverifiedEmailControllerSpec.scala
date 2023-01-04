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
import config.AppConfig
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.{undeliverable_email, unverified_email}

class UnverifiedEmailControllerSpec extends ControllerWithoutFormSpec {

  val unverifiedPage = mock[unverified_email]
  val undeliverablePage = mock[undeliverable_email]

  def controller() =
    new UnverifiedEmailController(mockAuthAction, stubMessagesControllerComponents(), unverifiedPage, undeliverablePage, mock[AppConfig])

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
  }

  "UnverifiedEmailController" should {
    "display the unverified email detection page" in {
      when(unverifiedPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
      val result = controller().informUserUnverified(getRequest())

      status(result) mustBe OK
    }

    "display the undeliverable email detection page" in {
      when(undeliverablePage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
      val result = controller().informUserUndeliverable(getRequest())

      status(result) mustBe OK
    }
  }
}
