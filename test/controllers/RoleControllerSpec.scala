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

package controllers

import base.CustomExportsBaseSpec
import forms.Role
import org.scalatest.BeforeAndAfter
import play.api.test.Helpers._

class RoleControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  val roleUri = uriWithContextPath("/role")

  before {
    authorizedUser()
  }

  "RoleController on displayRolePage" should {

    "return 200 code" in {
      withCaching[Role](None, Role.roleId)

      val result = route(app, getRequest(roleUri)).get

      status(result) must be(OK)
    }

    "display form for Role" in {
      withCaching[Role](None, Role.roleId)

      val result = route(app, getRequest(roleUri)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("movement.role.description"))
      stringResult must include(messages("movement.role.dec"))
      stringResult must include(messages("movement.role.drep"))
      stringResult must include(messages("movement.role.irep"))
    }
  }

}
