/*
 * Copyright 2023 HM Revenue & Customs
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

import base.ControllerWithoutFormSpec
import models.declaration.submissions.RequestType.SubmissionRequest
import models.declaration.submissions.{Action, Submission}
import models.requests.ExportsSessionKeys
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.dashboard.DashboardHelper.toDashboard
import views.html.declaration_details

import java.time.ZonedDateTime
import java.util.UUID
import scala.concurrent.Future

class DeclarationDetailsControllerSpec extends ControllerWithoutFormSpec with BeforeAndAfterEach with OptionValues {

  private val actionId = "actionId"

  private val submission = {
    val uuid = UUID.randomUUID().toString
    Submission(
      uuid = uuid,
      eori = "eori",
      lrn = "lrn",
      mrn = Some("mrn"),
      ducr = Some("ducr"),
      actions =
        Seq(Action(id = actionId, requestType = SubmissionRequest, requestTimestamp = ZonedDateTime.now, notifications = None, Some(uuid), 1)),
      latestDecId = Some(uuid)
    )
  }

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
      "submission but no notifications are provided for the Declaration" in {
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submission)))

        val result = controller.displayPage(actionId)(getRequest())
        status(result) mustBe OK

        session(result).get(ExportsSessionKeys.submissionId).value mustBe submission.uuid
        session(result).get(ExportsSessionKeys.submissionMrn).value mustBe submission.mrn.value
        session(result).get(ExportsSessionKeys.submissionLrn).value mustBe submission.lrn
        session(result).get(ExportsSessionKeys.submissionDucr).value mustBe submission.ducr.value

        val submissionCaptor: ArgumentCaptor[Submission] = ArgumentCaptor.forClass(classOf[Submission])
        verify(declarationDetailsPage).apply(submissionCaptor.capture())(any(), any())
        submissionCaptor.getValue mustBe submission
      }
    }

    "return 303 (SEE_OTHER)" when {
      "there is no submission for the Declaration" in {
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any())).thenReturn(Future.successful(None))

        val result = controller.displayPage(actionId)(getRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe toDashboard.url

        verifyNoInteractions(declarationDetailsPage)
      }
    }
  }
}
