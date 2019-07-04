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

package controllers.declaration

import base.CustomExportsBaseSpec
import forms.Choice
import forms.Choice.choiceId
import helpers.views.declaration.ConfirmationMessages
import play.api.test.Helpers._

class ConfirmationPageControllerSpec extends CustomExportsBaseSpec with ConfirmationMessages {

  override def beforeEach() {
    authorizedUser()
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  "Confirmation Controller on GET" should {

    "return 200 status code" in {

      val result = route(app, getRequest(uriWithContextPath("/declaration/confirmation"))).get

      status(result) must be(OK)
    }
  }
}
