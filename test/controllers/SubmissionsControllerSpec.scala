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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import play.api.test.Helpers._

class SubmissionsControllerSpec extends CustomExportsBaseSpec {

  val notificationsUri = uriWithContextPath("/notifications")
  val submissionsUri = uriWithContextPath("/submissions")

  "Submissions Controller" should {

    "return 200 code" in {

      authorizedUser()
      listOfNotifications()
      listOfSubmissions()

      val result = route(app, getRequest(submissionsUri)).get

      status(result) must be(OK)
    }

    "display submissions page with number of notifications for each submission" in {

      authorizedUser()
      listOfNotifications()
      listOfSubmissions()

      val result = route(app, getRequest(submissionsUri)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("submissions.title"))
      stringResult must include(messages("submissions.ducr"))
      stringResult must include(messages("submissions.lrn"))
      stringResult must include(messages("submissions.mrn"))
      stringResult must include(messages("submissions.submittedTimestamp"))
      stringResult must include(messages("submissions.status"))
      stringResult must include(messages("submissions.noOfNotifications"))
    }

    "call CustomsDeclareExportsConnector 2 times: for submissions and for notifications" in {

      authorizedUser()
      listOfNotifications()
      listOfSubmissions()

      val result = route(app, getRequest(submissionsUri)).get

      status(result) must be(OK)

      val inOrderCheck = Mockito.inOrder(mockCustomsDeclareExportsConnector)
      inOrderCheck.verify(mockCustomsDeclareExportsConnector).fetchSubmissions()(any(), any())
      inOrderCheck.verify(mockCustomsDeclareExportsConnector).fetchNotifications()(any(), any())
    }
  }

}
