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

import base.ControllerSpec
import controllers.declaration.routes.SummaryController
import controllers.routes.{CopyDeclarationController, DeclarationDetailsController}
import forms.{CopyDeclaration, Ducr, Lrn, LrnValidator}
import mock.ErrorHandlerMocks
import models.DeclarationType.STANDARD
import models.declaration.DeclarationStatus.DRAFT
import models.declaration.submissions.EnhancedStatus
import models.declaration.submissions.EnhancedStatus.{rejectedStatuses, CLEARED}
import models.requests.SessionHelper
import models.requests.SessionHelper.{submissionDucr, submissionLrn, submissionMrn, submissionUuid}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.{GivenWhenThen, OptionValues}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import testdata.SubmissionsTestData.submission
import views.html.copy_declaration

import scala.concurrent.Future

class CopyDeclarationControllerSpec extends ControllerSpec with ErrorHandlerMocks with GivenWhenThen with OptionValues {

  private val lrnValidator = mock[LrnValidator]
  private val copyDeclarationPage = mock[copy_declaration]

  val controller = new CopyDeclarationController(
    mockAuthAction,
    mockVerifiedEmailAction,
    mockJourneyAction,
    mockErrorHandler,
    mockCustomsDeclareExportsConnector,
    mockExportsCacheService,
    lrnValidator,
    stubMessagesControllerComponents(),
    copyDeclarationPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    setupErrorHandler()

    authorizedUser()
    when(lrnValidator.hasBeenSubmittedInThePast48Hours(any[Lrn])(any(), any())).thenReturn(Future.successful(false))
    when(copyDeclarationPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(lrnValidator, copyDeclarationPage)
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(request))
    theResponseForm
  }

  def theResponseForm: Form[CopyDeclaration] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[CopyDeclaration]])
    verify(copyDeclarationPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  private val correctForm = Json.toJson(CopyDeclaration(Ducr(DUCR), LRN))

  "CopyDeclarationController.redirectToReceiveJourneyRequest" should {

    "redirect to /submissions/:id/information" when {
      rejectedStatuses.foreach { enhancedStatus =>
        s"the Submission document was found but lastEnhancedStatus is $enhancedStatus" in {
          val rejectedSubmission = submission.copy(latestEnhancedStatus = Some(enhancedStatus))
          fetchSubmission(submission.uuid, rejectedSubmission)

          val result = controller.redirectToReceiveJourneyRequest(rejectedSubmission.uuid)(FakeRequest("GET", ""))

          status(result) must be(SEE_OTHER)
          redirectLocation(result) mustBe Some(DeclarationDetailsController.displayPage(rejectedSubmission.uuid).url)
        }
      }
    }

    "return 500 (InternalServerError)" when {
      "the Submission document was not found" in {
        submissionNotFound

        val result = controller.redirectToReceiveJourneyRequest("submissionUuid")(FakeRequest("GET", ""))
        status(result) must be(INTERNAL_SERVER_ERROR)
      }
    }

    "redirect to /copy-declaration" when {
      "the Submission document was found and" when {
        EnhancedStatus.values.filterNot(rejectedStatuses.contains).foreach { enhancedStatus =>
          s"lastEnhancedStatus is $enhancedStatus" in {
            val nonRejectedSubmission = submission.copy(latestEnhancedStatus = Some(enhancedStatus))
            fetchSubmission(submission.uuid, nonRejectedSubmission)

            val result = controller.redirectToReceiveJourneyRequest(nonRejectedSubmission.uuid)(FakeRequest("GET", ""))

            status(result) must be(SEE_OTHER)
            redirectLocation(result) mustBe Some(CopyDeclarationController.displayPage.url)
            val ss = session(result)
            ss.get(SessionHelper.declarationUuid) mustBe Some(nonRejectedSubmission.uuid)
            assert(List(submissionDucr, submissionUuid, submissionLrn, submissionMrn).forall(ss.get(_) == None))
          }
        }
      }
    }
  }

  "CopyDeclarationController.displayPage" should {
    onStandard { request =>
      "return 200 (OK)" in {
        withNewCaching(request.cacheModel)

        val result = controller.displayPage(getRequest())
        status(result) must be(OK)
      }
    }
  }

  "CopyDeclarationController.submitPage" should {

    "return 400 (BAD_REQUEST)" when {

      "user enters incorrect data" in {
        withNewCaching(withRequestOfType(STANDARD).cacheModel)
        val incorrectForm = Json.toJson(CopyDeclaration(Ducr("1234"), Lrn("")))

        val result = controller.submitPage(postRequest(incorrectForm))
        status(result) must be(BAD_REQUEST)
      }

      "LrnValidator returns false" in {
        when(lrnValidator.hasBeenSubmittedInThePast48Hours(any[Lrn])(any(), any())).thenReturn(Future.successful(true))
        withNewCaching(withRequestOfType(STANDARD).cacheModel)
        val correctForm = Json.toJson(CopyDeclaration(Ducr(DUCR), LRN))

        val result = controller.submitPage(postRequest(correctForm))
        status(result) must be(BAD_REQUEST)
      }
    }

    "return 500 (InternalServerError)" when {

      "the Submission document was not found" in {
        withNewCaching(withRequestOfType(STANDARD).cacheModel)
        submissionNotFound

        val result = controller.submitPage(postRequest(correctForm))
        status(result) must be(INTERNAL_SERVER_ERROR)
      }

      "the Submission document was found but latestDecId is undefined" in {
        withNewCaching(withRequestOfType(STANDARD, withId(submission.uuid)).cacheModel)
        fetchSubmission(submission.uuid, submission.copy(latestDecId = None))

        val result = controller.submitPage(postRequest(correctForm))
        status(result) must be(INTERNAL_SERVER_ERROR)
      }

      "the ExportsDeclaration document to copy was not found" in {
        withNewCaching(withRequestOfType(STANDARD, withId(submission.uuid)).cacheModel)
        fetchSubmission(submission.uuid, submission)
        declarationNotFound

        val result = controller.submitPage(postRequest(correctForm))
        status(result) must be(INTERNAL_SERVER_ERROR)
      }
    }

    "change to uppercase any lowercase letter entered in the DUCR field" in {
      withNewCaching(withRequestOfType(STANDARD, withId(submission.uuid)).cacheModel)
      fetchSubmission(submission.uuid, submission)
      fetchDeclaration(submission.latestDecId.value)

      val ducr = "9gb123456664559-1abc"
      val correctForm = Json.toJson(CopyDeclaration(Ducr(ducr), LRN))
      val result = controller.submitPage(postRequest(correctForm))

      And("return 303 (SEE_OTHER)")
      status(result) must be(SEE_OTHER)

      val declaration = theCacheModelCreated
      declaration.consignmentReferences.head.ducr.get.ducr mustBe ducr.toUpperCase
      declaration.consignmentReferences.head.lrn mustBe Some(LRN)
      declaration.consignmentReferences.head.mrn mustBe None
      declaration.consignmentReferences.head.eidrDateStamp mustBe None
    }

    "populate the new declaration" in {
      val subWithEnhancedStatus = submission.copy(latestEnhancedStatus = Some(CLEARED))
      withNewCaching(withRequestOfType(STANDARD, withId(subWithEnhancedStatus.uuid)).cacheModel)
      fetchSubmission(subWithEnhancedStatus.uuid, subWithEnhancedStatus)

      val declarationId = subWithEnhancedStatus.latestDecId.value
      val latestDeclaration = aDeclaration(withId(declarationId))
      fetchDeclaration(declarationId, latestDeclaration)

      val result = controller.submitPage(postRequest(correctForm))

      status(result) must be(SEE_OTHER)
      redirectLocation(result) mustBe Some(SummaryController.displayPage.url)

      val declaration = theCacheModelCreated
      declaration.declarationMeta.parentDeclarationId mustBe Some(declarationId)
      declaration.declarationMeta.parentDeclarationEnhancedStatus mustBe Some(CLEARED)
      declaration.declarationMeta.status mustBe DRAFT
      declaration.linkDucrToMucr mustBe None
      declaration.mucr mustBe None
    }

    "redirect to /saved-summary" in {
      withNewCaching(withRequestOfType(STANDARD, withId(submission.uuid)).cacheModel)
      fetchSubmission(submission.uuid, submission)
      fetchDeclaration(submission.latestDecId.value)

      val result = controller.submitPage(postRequest(correctForm))

      status(result) must be(SEE_OTHER)
      redirectLocation(result) mustBe Some(SummaryController.displayPage.url)
    }
  }
}
