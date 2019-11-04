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

import controllers.declaration.NotEligibleController
import models.DeclarationType
import play.api.test.Helpers._
import unit.base.ControllerSpec
import views.html.declaration.not_eligible

class NotEligibleControllerSpec extends ControllerSpec {

  trait SetUp {
    val notEligiblePage = new not_eligible(mainTemplate)

    val controller =
      new NotEligibleController(mockAuthAction, mockJourneyAction, stubMessagesControllerComponents(), notEligiblePage)(ec)

    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))
  }

  "Not Eligible Controller" should {

    "return 200 (OK)" when {

      "display method is invoked" in new SetUp {

        val result = controller.displayPage()(getRequest())

        status(result) must be(OK)
      }
    }
  }
}
