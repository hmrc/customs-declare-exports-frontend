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

import controllers.StartController
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerWithoutFormSpec
import views.html.start_page

class StartControllerSpec extends ControllerWithoutFormSpec {

  val mcc = stubMessagesControllerComponents()
  val startPage = mock[start_page]

  val controller = new StartController(mcc, startPage)(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(startPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(startPage)

    super.afterEach()
  }

  "Start Controller" should {

    "return 200" when {

      "display page method is invoked" in {

        val result = controller.displayStartPage()(getRequest())

        status(result) mustBe OK
      }
    }
  }
}
