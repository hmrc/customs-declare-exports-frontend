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

import controllers.SavedDeclarationsController
import play.api.test.Helpers._
import unit.base.ControllerSpec
import views.html.saved_declarations

class SavedDeclarationsControllerSpec extends ControllerSpec {

  trait SetUp {
    val savedDeclarationsPage = new saved_declarations(mainTemplate)

    val controller = new SavedDeclarationsController(
      mockAuthAction,
      mockCustomsDeclareExportsConnector,
      stubMessagesControllerComponents(),
      savedDeclarationsPage
    )(ec)

    authorizedUser()
  }

  "Submissions controller" should {

    "return 200 (OK)" when {

      "display declarations method is invoked" in new SetUp {

        listOfDraftDeclarations()

        val result = controller.displayDeclarations()(getRequest())

        status(result) must be(OK)
      }
    }

    "return 303 (SEE_OTHER)" when {
       "continue declaration method is invoked" in new SetUp {

         getDeclaration("123")

         val result = controller.continueDeclaration("123")(getRequest())

         status(result) must be(SEE_OTHER)
       }
    }
  }
}
