/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.amendments

import base.ControllerWithoutFormSpec
import controllers.helpers.AmendmentHelper
import models.DeclarationMeta
import models.declaration.DeclarationStatus.COMPLETE
import models.declaration.submissions.Action
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString, refEq}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.{Assertion, OptionValues}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import testdata.SubmissionsTestData.{action, submission}
import views.html.amendments.{amendment_details, unavailable_amendment_details}

import java.time.Instant
import scala.concurrent.Future

class AmendmentDetailsControllerSpec extends ControllerWithoutFormSpec with OptionValues {

  private val amendmentDetails = mock[amendment_details]
  private val unavailableAmendmentDetails = mock[unavailable_amendment_details]
  private val amendmentHelper = mock[AmendmentHelper]

  val controller = new AmendmentDetailsController(
    mockAuthAction,
    mockVerifiedEmailAction,
    mcc,
    mockErrorHandler,
    mockCustomsDeclareExportsConnector,
    amendmentDetails,
    unavailableAmendmentDetails,
    amendmentHelper
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    setupErrorHandler()

    when(amendmentDetails.apply(any(), any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(unavailableAmendmentDetails.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(amendmentDetails, mockCustomsDeclareExportsConnector, mockErrorHandler, unavailableAmendmentDetails)
  }

  private val request = FakeRequest()
  private val declarationMeta = DeclarationMeta(Some("parentDeclId"), None, COMPLETE, Instant.now(), Instant.now())

  "AmendmentDetailsController.displayPage" should {

    "return 500 (INTERNAL_SERVER-ERROR)" when {

      "no Submission is found for the given action.id" in {
        when(mockCustomsDeclareExportsConnector.findSubmissionByAction(anyString())(any(), any()))
          .thenReturn(Future.successful(None))

        verifyError("For the given actionId(actionId) on /amendment-details, the related Submission was not found")
      }

      "the fetched Submission does not contain an Action with the given actionId" in {
        when(mockCustomsDeclareExportsConnector.findSubmissionByAction(anyString())(any(), any()))
          .thenReturn(Future.successful(Some(submission)))

        verifyError("For the given actionId(actionId) on /amendment-details, the related Submission has not such action")
      }

      "no declaration is found with the given action.decId" in {
        when(mockCustomsDeclareExportsConnector.findSubmissionByAction(anyString())(any(), any()))
          .thenReturn(Future.successful(Some(submission)))

        when(mockCustomsDeclareExportsConnector.findDeclaration(anyString())(any(), any()))
          .thenReturn(Future.successful(None))

        val actionId = submission.actions(0).id
        val message = s"No Declaration found for Action($actionId) on /amendment-details"
        verifyError(message, actionId)
      }

      "the fetched declaration has no parentDeclarationId" in {
        when(mockCustomsDeclareExportsConnector.findSubmissionByAction(anyString())(any(), any()))
          .thenReturn(Future.successful(Some(submission)))

        val declaration = aDeclaration()
        when(mockCustomsDeclareExportsConnector.findDeclaration(anyString())(any(), any()))
          .thenReturn(Future.successful(Some(declaration)))

        val action = submission.actions(0)
        val message = s"No parentDeclarationId for Declaration(${action.decId.value}) on /amendment-details"
        verifyError(message, action.id)
      }

      "no parentDeclaration is found for the fetched declaration" in {
        when(mockCustomsDeclareExportsConnector.findSubmissionByAction(anyString())(any(), any()))
          .thenReturn(Future.successful(Some(submission)))

        val action = submission.actions(0)
        val declaration = aDeclaration().copy(declarationMeta = declarationMeta)

        when(mockCustomsDeclareExportsConnector.findDeclaration(refEq(action.decId.value))(any(), any()))
          .thenReturn(Future.successful(Some(declaration)))

        when(mockCustomsDeclareExportsConnector.findDeclaration(refEq("parentDeclId"))(any(), any()))
          .thenReturn(Future.successful(None))

        val message = s"No parent Declaration found for Declaration(${action.decId.value}) on /amendment-details"
        verifyError(message, action.id)
      }

      def verifyError(message: String, actionId: String = "actionId"): Assertion = {
        val result = controller.displayPage(actionId)(request)
        status(result) mustBe INTERNAL_SERVER_ERROR

        val messageCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
        verify(mockErrorHandler).internalError(messageCaptor.capture())(any())
        assert(messageCaptor.getValue.startsWith(message))
      }
    }

    "render the 'unavailable_amendment_details' page when Action.decId is not defined" in {
      val fetchedSubmission = submission.copy(actions = List(action.copy(decId = None)))
      when(mockCustomsDeclareExportsConnector.findSubmissionByAction(anyString())(any(), any()))
        .thenReturn(Future.successful(Some(fetchedSubmission)))

      val result = controller.displayPage(submission.actions(0).id)(request)
      status(result) mustBe OK

      val captor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      verify(unavailableAmendmentDetails).apply(captor.capture(), any())(any(), any())
      captor.getValue mustBe submission.uuid
    }

    "render the 'amendment_details' page when Action.decId is defined" in {
      when(mockCustomsDeclareExportsConnector.findSubmissionByAction(anyString())(any(), any()))
        .thenReturn(Future.successful(Some(submission)))

      val declaration = aDeclaration(withConsignmentReferences()).copy(declarationMeta = declarationMeta)
      when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
        .thenReturn(Future.successful(Some(declaration)))

      val result = controller.displayPage(submission.actions(0).id)(request)
      status(result) mustBe OK

      val submissionCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val ducrCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val actionCaptor: ArgumentCaptor[Action] = ArgumentCaptor.forClass(classOf[Action])
      verify(amendmentDetails).apply(submissionCaptor.capture(), ducrCaptor.capture(), any(), actionCaptor.capture(), any())(any(), any())
      submissionCaptor.getValue mustBe submission.uuid
      ducrCaptor.getValue mustBe DUCR
      actionCaptor.getValue.id mustBe submission.actions(0).id
    }
  }
}
