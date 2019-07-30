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

package unit.controllers.declaration

import controllers.declaration.ConfirmationController
import forms.Choice
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import play.api.test.Helpers._
import unit.base.ControllerSpec
import views.html.declaration.confirmation_page

class ConfirmationControllerSpec extends ControllerSpec {

  trait SetUp {
    val confirmationPage = new confirmation_page(mainTemplate)

    val controller = new ConfirmationController(
      mockAuthAction,
      mockJourneyAction,
      stubMessagesControllerComponents(),
      confirmationPage
    )(ec, minimalAppConfig)

    authorizedUser()
    withNewCaching(aCacheModel(withChoice(SupplementaryDec)))
  }

  "Confirmation Controller on GET" should {

    "return 200 status code" in new SetUp {

      val result = controller.displayPage()(getRequest())

      status(result) must be(OK)
    }
  }
}
