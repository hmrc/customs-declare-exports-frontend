/*
 * Copyright 2019 HM Revenue & Customs
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
import play.api.test.Helpers._
import unit.base.ControllerSpec
import views.html.start_page

class StartControllerSpec extends ControllerSpec {

  val mcc = stubMessagesControllerComponents()
  val startPage = new start_page(mainTemplate)

  val controller = new StartController(mcc, startPage)(ec, minimalAppConfig)

  "Start Controller" should {

    "return 200" when {

      "display page method is invoked" in {

        val result = controller.displayStartPage()(getRequest())

        status(result) must be(OK)
      }
    }
  }
}
