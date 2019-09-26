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

import controllers.{routes, RejectedNotificationsController}
import models.declaration.submissions.RequestType.SubmissionRequest
import models.declaration.submissions.{Action, Submission}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.rejected_notification_errors

import scala.concurrent.ExecutionContext.global

class RejectedNotificationsControllerSpec extends ControllerSpec with OptionValues {

  private val mockRejectedNotificationPage = mock[rejected_notification_errors]

  private val controller = new RejectedNotificationsController(
    mockAuthAction,
    mockCustomsDeclareExportsConnector,
    stubMessagesControllerComponents(),
    mockRejectedNotificationPage
  )(global)

  private val submissionId = "SubmissionId"
  private val action = Action("convId", SubmissionRequest)
  private val submission = Submission(submissionId, "eori", "lrn", actions = Seq(action))

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(mockRejectedNotificationPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockRejectedNotificationPage)

    super.afterEach()
  }

  "Rejected Notification Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked with submission and notifications" in {

        findSubmission(submissionId, Some(submission))
        findNotifications(submissionId)

        val result = controller.displayPage(submissionId)(getRequest())

        status(result) mustBe OK
        verify(mockRejectedNotificationPage).apply(any(), any())(any(), any())
      }
    }

    "return 303 (SEE_OTHER) and redirect to Submission Controller" when {

      "display page method is invoked without submission" in {

        findSubmission(submissionId, None)

        val result = controller.displayPage(submissionId)(getRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.SubmissionsController.displayListOfSubmissions().url
      }
    }
  }
}
