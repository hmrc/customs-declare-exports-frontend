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

import controllers.SubmissionsController
import play.api.test.Helpers._
import unit.base.ControllerSpec
import views.html.submissions

class SubmissionsControllerSpec extends ControllerSpec {

  trait SetUp {
    val submissionsPage = new submissions(mainTemplate)

    val controller = new SubmissionsController(
      mockAuthAction,
      mockCustomsDeclareExportsConnector,
      stubMessagesControllerComponents(),
      submissionsPage
    )(ec, minimalAppConfig)

    authorizedUser()
  }

  "Submissions controller" should {

    "return 200 (OK)" when {

      "display page method is invoked" in new SetUp {

        listOfSubmissions()
        listOfNotifications()

        val result = controller.displayListOfSubmissions()(getRequest())

        status(result) must be(OK)
      }
    }
  }
}
