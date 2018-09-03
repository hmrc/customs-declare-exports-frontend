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

import base.SpecBase
import play.api.test.Helpers._

class SimpleDeclarationControllerSpec extends SpecBase{

  val uri = uriWithContextPath("/simple-declaration")

  "SimpleDeclarationSpec" should {
    "process only authenticated user requests " in {
      authroizedUser()
      val result = (route(app, getRequest(uri)))
      result.map(status(_) must be (OK))
    }
    "return 200 with a success" in {
      authroizedUser()
      val result = (route(app, getRequest(uri)))
      result.map(status(_) must be (OK))
    }

    "display Simple- declaration" in {

    }

    "should validate form  submitted" in {

    }

    "should redirect to next page" in {

    }
  }
}
