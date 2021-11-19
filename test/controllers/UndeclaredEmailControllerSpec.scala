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
import config.AppConfig
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.undeclared_email

class UndeclaredEmailControllerSpec extends ControllerWithoutFormSpec {

  val page = mock[undeclared_email]

  def controller() =
    new UndeclaredEmailController(mockAuthAction, stubMessagesControllerComponents(), page, mock[AppConfig])

  override protected def beforeEach(): Unit = {
    super.beforeEach

    reset(page)

    authorizedUser()
    when(page.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  "UndeclaredEmailController" should {
    "display the undeclared email detection page" in {
      val result = controller().informUser(getRequest())

      status(result) mustBe OK
    }
  }
}
