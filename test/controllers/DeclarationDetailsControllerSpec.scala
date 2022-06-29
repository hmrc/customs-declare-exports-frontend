/*
 * Copyright 2022 HM Revenue & Customs
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

import java.time.ZonedDateTime
import java.util.UUID

import scala.concurrent.Future

import base.ControllerWithoutFormSpec
import models.declaration.notifications.Notification
import models.declaration.submissions.RequestType.SubmissionRequest
import models.declaration.submissions.{Action, Submission, SubmissionStatus}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration_details

class DeclarationDetailsControllerSpec extends ControllerWithoutFormSpec with BeforeAndAfterEach {

  private val actionId = "actionId"
  private val notification = Notification(actionId, "mrn", ZonedDateTime.now, SubmissionStatus.UNKNOWN, Seq.empty)

  private val submission = Submission(
    uuid = UUID.randomUUID().toString,
    eori = "eori",
    lrn = "lrn",
    mrn = None,
    ducr = None,
    actions = Seq(Action(id = actionId, requestType = SubmissionRequest, requestTimestamp = ZonedDateTime.now, notifications = None))
  )

  private val declarationDetailsPage = mock[declaration_details]

  val controller = new DeclarationDetailsController(
    mockAuthAction,
    mockVerifiedEmailAction,
    mockCustomsDeclareExportsConnector,
    stubMessagesControllerComponents(),
    declarationDetailsPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(declarationDetailsPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit =
    reset(declarationDetailsPage, mockCustomsDeclareExportsConnector)

  "displayPage method of Declaration Details page" should {

    "return 200 (OK)" when {
      val submissionCaptor: ArgumentCaptor[Submission] = ArgumentCaptor.forClass(classOf[Submission])

      "submission but no notifications are provided for the Declaration" in {
        responsesToReturn(isQueryNotificationMessageEnabled = true, List.empty)

        val result = controller.displayPage(actionId)(getRequest())
        status(result) mustBe OK

        verify(declarationDetailsPage).apply(submissionCaptor.capture())(any(), any())
        submissionCaptor.getValue mustBe submission
      }

      def responsesToReturn(
        isQueryNotificationMessageEnabled: Boolean,
        notifications: Seq[Notification] = List(notification)
      ): OngoingStubbing[Future[Option[Submission]]] =
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any())).thenReturn(Future.successful(Some(submission)))
    }

    "return 303 (SEE_OTHER)" when {

      "there is no submission for the Declaration" in {

        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any())).thenReturn(Future.successful(None))

        val result = controller.displayPage(actionId)(getRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe routes.SubmissionsController.displayListOfSubmissions().url

        verifyNoInteractions(declarationDetailsPage)
      }
    }
  }
}
