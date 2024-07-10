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

package controllers.timeline

import base.ControllerWithoutFormSpec
import forms.section1.additionaldeclarationtype.AdditionalDeclarationType.{AdditionalDeclarationType, STANDARD_FRONTIER}
import models.declaration.submissions.RequestType.{ExternalAmendmentRequest, SubmissionRequest}
import models.declaration.submissions.{Action, Submission}
import models.requests.SessionHelper
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.timeline.{declaration_details, unavailable_timeline_actions}

import java.time.ZonedDateTime
import java.util.UUID
import scala.concurrent.Future

class DeclarationDetailsControllerSpec extends ControllerWithoutFormSpec with BeforeAndAfterEach with OptionValues {

  private val uuid = UUID.randomUUID().toString
  private val action = Action("actionId", SubmissionRequest, ZonedDateTime.now, notifications = None, Some(uuid), 1)

  private val submission =
    Submission(uuid = uuid, eori = "eori", lrn = "lrn", mrn = Some("mrn"), ducr = Some("ducr"), actions = List(action), latestDecId = Some(uuid))

  private val declarationDetailsPage = mock[declaration_details]
  private val unavailableTimelineActionsPage = mock[unavailable_timeline_actions]

  val controller = new DeclarationDetailsController(
    mockAuthAction,
    mockVerifiedEmailAction,
    mockErrorHandler,
    mockCustomsDeclareExportsConnector,
    mcc,
    declarationDetailsPage,
    unavailableTimelineActionsPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    setupErrorHandler()
    when(declarationDetailsPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(unavailableTimelineActionsPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit =
    reset(declarationDetailsPage, mockCustomsDeclareExportsConnector, mockErrorHandler, unavailableTimelineActionsPage)

  "DeclarationDetailsController.displayPage" should {

    "return 200 (OK)" when {
      "submission but no notifications are provided for the Declaration" in {
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submission)))

        val expectedDeclarationType = STANDARD_FRONTIER
        val declaration = aDeclaration(withAdditionalDeclarationType(expectedDeclarationType))
        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(Some(declaration)))

        val result = controller.displayPage(submission.uuid)(getAuthenticatedRequest())
        status(result) mustBe OK

        session(result).get(SessionHelper.submissionUuid).value mustBe submission.uuid
        session(result).get(SessionHelper.submissionMrn).value mustBe submission.mrn.value
        session(result).get(SessionHelper.submissionLrn).value mustBe submission.lrn
        session(result).get(SessionHelper.submissionDucr).value mustBe submission.ducr.value

        val submissionCaptor: ArgumentCaptor[Submission] = ArgumentCaptor.forClass(classOf[Submission])
        val declarationTypeCaptor: ArgumentCaptor[AdditionalDeclarationType] = ArgumentCaptor.forClass(classOf[AdditionalDeclarationType])
        verify(declarationDetailsPage).apply(submissionCaptor.capture(), declarationTypeCaptor.capture())(any(), any())
        submissionCaptor.getValue mustBe submission
        declarationTypeCaptor.getValue mustBe expectedDeclarationType
      }
    }

    "fetch the correct declaration according to the expected declarationId" when {
      val declaration = aDeclaration(withAdditionalDeclarationType())

      "the declaration was NOT externally amended" in {
        val expectedDeclarationId = "latestDecId"
        val notAmendedSubmission = submission.copy(latestDecId = Some(expectedDeclarationId))

        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(notAmendedSubmission)))

        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(Some(declaration)))

        val result = controller.displayPage(notAmendedSubmission.uuid)(getAuthenticatedRequest())
        status(result) mustBe OK

        val declarationIdCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
        verify(mockCustomsDeclareExportsConnector).findDeclaration(declarationIdCaptor.capture())(any(), any())
        declarationIdCaptor.getValue mustBe expectedDeclarationId
      }

      "the declaration was externally amended" in {
        val action = Action("actionId", ExternalAmendmentRequest, ZonedDateTime.now, None, None, 2)
        val amendedSubmission = submission.copy(actions = List(action))

        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(amendedSubmission)))

        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(Some(declaration)))

        val result = controller.displayPage(amendedSubmission.uuid)(getAuthenticatedRequest())
        status(result) mustBe OK

        val declarationIdCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
        verify(mockCustomsDeclareExportsConnector).findDeclaration(declarationIdCaptor.capture())(any(), any())
        declarationIdCaptor.getValue mustBe amendedSubmission.uuid
      }
    }

    "return 500 (INTERNAL_SERVER-ERROR)" when {
      "there is no submission for the Declaration" in {
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any())).thenReturn(Future.successful(None))

        val result = controller.displayPage(submission.uuid)(getAuthenticatedRequest())

        status(result) mustBe INTERNAL_SERVER_ERROR

        val messageCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
        verify(mockErrorHandler).internalError(messageCaptor.capture())(any())
        assert(messageCaptor.getValue.contains(s"Cannot found Submission(${submission.uuid})"))
      }

      "the declaration was not externally amended and" when {
        "'latestDecId' in the given submission is not defined" in {
          when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
            .thenReturn(Future.successful(Some(submission.copy(latestDecId = None))))

          val result = controller.displayPage(submission.uuid)(getAuthenticatedRequest())

          status(result) mustBe INTERNAL_SERVER_ERROR
          verify(mockCustomsDeclareExportsConnector, never).findDeclaration(any())(any(), any())

          val messageCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
          verify(mockErrorHandler).internalServerError(messageCaptor.capture())(any())
          assert(messageCaptor.getValue.contains("undefined latestDecId"))
        }
      }

      "the latest declaration, given the submission, cannot be fetched" in {
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submission)))

        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(None))

        val result = controller.displayPage(submission.uuid)(getAuthenticatedRequest())

        status(result) mustBe INTERNAL_SERVER_ERROR

        val messageCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
        verify(mockErrorHandler).internalServerError(messageCaptor.capture())(any())
        assert(messageCaptor.getValue.contains("Cannot found latest declaration"))
      }

      "the additional declaration type of the declaration is not defined" in {
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submission)))

        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(Some(aDeclaration())))

        val result = controller.displayPage(submission.uuid)(getAuthenticatedRequest())

        status(result) mustBe INTERNAL_SERVER_ERROR

        val messageCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
        verify(mockErrorHandler).internalServerError(messageCaptor.capture())(any())
        assert(messageCaptor.getValue.contains("has no additionalDeclarationType"))
      }
    }
  }

  "DeclarationDetailsController.unavailableActions" should {
    "return 200 (OK)" in {
      val result = controller.unavailableActions(submission.uuid)(getAuthenticatedRequest())
      status(result) mustBe OK
    }
  }
}
