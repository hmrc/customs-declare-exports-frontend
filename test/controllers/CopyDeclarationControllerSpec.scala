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

import base.ControllerSpec
import controllers.routes.{CopyDeclarationController, DeclarationDetailsController}
import controllers.declaration.routes.SummaryController
import forms.{CopyDeclaration, Ducr, Lrn, LrnValidator}
import models.DeclarationStatus.DRAFT
import models.declaration.submissions.EnhancedStatus
import models.declaration.submissions.EnhancedStatus.rejectedStatuses
import models.requests.ExportsSessionKeys
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.{clearInvocations, reset, verify, when}
import org.scalatest.GivenWhenThen
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import testdata.SubmissionsTestData.submission
import views.html.copy_declaration

import scala.concurrent.Future

class CopyDeclarationControllerSpec extends ControllerSpec with GivenWhenThen {

  private val lrnValidator = mock[LrnValidator]
  private val copyDeclarationPage = mock[copy_declaration]

  val controller = new CopyDeclarationController(
    mockAuthAction,
    mockVerifiedEmailAction,
    mockJourneyAction,
    mockCustomsDeclareExportsConnector,
    mockExportsCacheService,
    lrnValidator,
    stubMessagesControllerComponents(),
    copyDeclarationPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

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

  "CopyDeclarationController.redirectToReceiveJourneyRequest" should {

    "redirect to /submissions/:id/information" when {

      "the Submission document was not found" in {
        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any())).thenReturn(Future.successful(None))

        val submissionId = "submissionId"
        val result = controller.redirectToReceiveJourneyRequest(submissionId)(FakeRequest("GET", ""))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) mustBe Some(DeclarationDetailsController.displayPage(submissionId).url)
      }

      rejectedStatuses.foreach { enhancedStatus =>
        s"the Submission document was found but lastEnhancedStatus is $enhancedStatus" in {
          val rejectedSubmission = submission.copy(latestEnhancedStatus = Some(enhancedStatus))
          when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
            .thenReturn(Future.successful(Some(rejectedSubmission)))

          val result = controller.redirectToReceiveJourneyRequest(rejectedSubmission.uuid)(FakeRequest("GET", ""))

          status(result) must be(SEE_OTHER)
          redirectLocation(result) mustBe Some(DeclarationDetailsController.displayPage(rejectedSubmission.uuid).url)
        }
      }
    }

    "redirect to /copy-declaration" when {
      "the Submission document was found and" when {
        EnhancedStatus.values.filterNot(rejectedStatuses.contains).foreach { enhancedStatus =>
          s"lastEnhancedStatus is $enhancedStatus" in {
            val nonRejectedSubmission = submission.copy(latestEnhancedStatus = Some(enhancedStatus))
            when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
              .thenReturn(Future.successful(Some(nonRejectedSubmission)))

            val result = controller.redirectToReceiveJourneyRequest(nonRejectedSubmission.uuid)(FakeRequest("GET", ""))

            status(result) must be(SEE_OTHER)
            redirectLocation(result) mustBe Some(CopyDeclarationController.displayPage.url)
            session(result).get(ExportsSessionKeys.declarationId) mustBe Some(nonRejectedSubmission.uuid)
          }
        }
      }
    }
  }

  "CopyDeclarationController.displayPage" should {
    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" in {
        withNewCaching(request.cacheModel)

        val result = controller.displayPage(getRequest())
        status(result) must be(OK)
      }
    }
  }

  "CopyDeclarationController.submitPage" should {

    onEveryDeclarationJourney() { request =>
      "return 400 (BAD_REQUEST)" when {

        "user enters incorrect data" in {
          withNewCaching(request.cacheModel)
          val incorrectForm = Json.toJson(CopyDeclaration(Ducr("1234"), Lrn("")))

          val result = controller.submitPage(postRequest(incorrectForm))
          status(result) must be(BAD_REQUEST)
        }

        "LrnValidator returns false" in {
          when(lrnValidator.hasBeenSubmittedInThePast48Hours(any[Lrn])(any(), any())).thenReturn(Future.successful(true))
          withNewCaching(request.cacheModel)
          val correctForm = Json.toJson(CopyDeclaration(Ducr(DUCR), LRN))

          val result = controller.submitPage(postRequest(correctForm))
          status(result) must be(BAD_REQUEST)
        }
      }

      "change to uppercase any lowercase letter entered in the DUCR field" in {
        withNewCaching(request.cacheModel)

        val ducr = "9gb123456664559-1abc"
        val correctForm = Json.toJson(CopyDeclaration(Ducr(ducr), LRN))
        val result = controller.submitPage(postRequest(correctForm))

        And("return 303 (SEE_OTHER)")
        status(result) must be(SEE_OTHER)

        val declaration = theCacheModelCreated
        declaration.consignmentReferences.head.ducr.ducr mustBe ducr.toUpperCase
        declaration.consignmentReferences.head.lrn mustBe Some(LRN)
        declaration.consignmentReferences.head.mrn mustBe None
        declaration.consignmentReferences.head.eidrDateStamp mustBe None
      }

      "redirect to /saved-summary" in {
        withNewCaching(request.cacheModel)

        val correctForm = Json.toJson(CopyDeclaration(Ducr(DUCR), LRN))
        val result = controller.submitPage(postRequest(correctForm))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) mustBe Some(SummaryController.displayPage().url)
      }
    }

    "lookup the declaration's related submission details" when {
      onStandard { request =>
        "submission found then use it to populate the new declaration" in {

          withNewCaching(request.cacheModel)

          clearInvocations(mockCustomsDeclareExportsConnector)

          val correctForm = Json.toJson(CopyDeclaration(Ducr(DUCR), LRN))
          val result = controller.submitPage(postRequest(correctForm))

          status(result) must be(SEE_OTHER)
          redirectLocation(result) mustBe Some(SummaryController.displayPage().url)

          verify(mockCustomsDeclareExportsConnector).findSubmission(refEq(request.cacheModel.id))(any(), any())

          val declaration = theCacheModelCreated
          declaration.parentDeclarationId mustBe Some(request.cacheModel.id)
          declaration.parentDeclarationEnhancedStatus mustBe Some(EnhancedStatus.UNKNOWN)
          declaration.status mustBe DRAFT
          declaration.linkDucrToMucr mustBe None
          declaration.mucr mustBe None
        }

        "no submission found leave new declaration's parentDeclarationEnhancedStatus empty" in {

          val sessionDecId = "noSuchSubmission"
          withNewCaching(request.cacheModel.copy(id = sessionDecId))

          clearInvocations(mockCustomsDeclareExportsConnector)
          when(mockCustomsDeclareExportsConnector.findSubmission(refEq(sessionDecId))(any(), any())).thenReturn(Future.successful(None))

          val correctForm = Json.toJson(CopyDeclaration(Ducr(DUCR), LRN))
          val result = controller.submitPage(postRequest(correctForm))

          status(result) must be(SEE_OTHER)
          redirectLocation(result) mustBe Some(SummaryController.displayPage().url)

          verify(mockCustomsDeclareExportsConnector).findSubmission(refEq(sessionDecId))(any(), any())

          val declaration = theCacheModelCreated
          declaration.parentDeclarationId mustBe Some(sessionDecId)
          declaration.parentDeclarationEnhancedStatus mustBe None
          declaration.status mustBe DRAFT
          declaration.linkDucrToMucr mustBe None
          declaration.mucr mustBe None
        }
      }
    }
  }
}
