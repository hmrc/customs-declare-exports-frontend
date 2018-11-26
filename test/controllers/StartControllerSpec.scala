/*
 * Copyright 2018 HM Revenue & Customs
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

import base.CustomExportsBaseSpec
import play.api.test.Helpers._

class StartControllerSpec extends CustomExportsBaseSpec{

  val uri = uriWithContextPath("/start-page")

  "StartController" should {
    "return 200 for a GET" in {
      val result = route(app, getRequest(uri)).get

      status(result) must be (OK)
    }
    "display radio button to start now" in {
      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("details of where you’re sending the export"))
      stringResult must include(messages("your Government Gateway details"))
      stringResult must include(messages("Your CHIEF ID"))
    }
  }

}
