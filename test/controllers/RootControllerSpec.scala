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
import controllers.routes.ChoiceController
import play.api.test.Helpers._

class RootControllerSpec extends ControllerWithoutFormSpec {

  val controller = new RootController(mcc)

  "Root Controller" should {
    "return 303" when {
      "display page method is invoked" in {
        val result = controller.displayPage(getRequest())
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(ChoiceController.displayPage.url)
      }
    }
  }
}
