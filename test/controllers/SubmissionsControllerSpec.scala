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

import base.{ControllerWithoutFormSpec, MockExportCacheService}
import config.PaginationConfig
import controllers.declaration.routes._
import models._
import models.declaration.submissions.EnhancedStatus.GOODS_ARRIVED
import models.declaration.submissions.RequestType.SubmissionRequest
import models.declaration.submissions.{Action, Submission}
import models.requests.SessionHelper.declarationUuid
import org.apache.pekko.util.Timeout
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.matchers.{BeMatcher, MatchResult}
import org.scalatest.{Assertion, BeforeAndAfterEach}
import play.api.mvc.Result
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import views.html.declaration.summary.submitted_declaration_page

import java.time.{Instant, ZoneOffset, ZonedDateTime}
import java.util.UUID
import scala.concurrent.Future

class SubmissionsControllerSpec extends ControllerWithoutFormSpec with MockExportCacheService with BeforeAndAfterEach {

  val dateTime = ZonedDateTime.now(ZoneOffset.UTC)

  val uuid = UUID.randomUUID().toString

  private val action =
    Action(
      requestType = SubmissionRequest,
      id = "conversationID",
      requestTimestamp = dateTime,
      notifications = None,
      decId = Some(uuid),
      versionNo = 1
    )

  private val submission =
    Submission(
      uuid = uuid,
      eori = "eori",
      lrn = "lrn",
      mrn = None,
      ducr = None,
      latestEnhancedStatus = Some(GOODS_ARRIVED),
      actions = Seq(action),
      latestDecId = Some(uuid)
    )

  private val submittedDeclarationPage = mock[submitted_declaration_page]
  private val paginationConfig = mock[PaginationConfig]

  val controller = new SubmissionsController(
    mockAuthAction,
    mockVerifiedEmailAction,
    mockExportsCacheService,
    mockCustomsDeclareExportsConnector,
    mockErrorHandler,
    mcc,
    submittedDeclarationPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    setupErrorHandler()
    authorizedUser()
    when(submittedDeclarationPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(paginationConfig.itemsPerPage).thenReturn(Page.DEFAULT_MAX_DOCUMENT_PER_PAGE)
  }

  override protected def afterEach(): Unit = {
    reset(submittedDeclarationPage, mockCustomsDeclareExportsConnector, paginationConfig, mockErrorHandler)
    super.afterEach()
  }

  super.afterEach()

  val throwable: Throwable = new IllegalArgumentException("Whoopse")

  "SubmissionsController on viewDeclaration" should {

    "return 200 (OK)" when {
      "there is a Declaration with given ID in the cache that has an associatedSubmissionId present that exists" in {
        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(Some(aDeclaration(withId("some-id"), withAssociatedSubmissionId(Some(submission.uuid))))))

        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(Some(submission)))

        val result = controller.viewDeclaration("some-id")(getRequest())

        status(result) mustBe OK
      }
    }

    "return 500 (INTERNAL_SERVER_ERROR)" when {
      "when findDeclaration method returns failed future" in {
        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.failed(throwable))

        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(None))

        val result = controller.viewDeclaration("some-id")(getRequest())
        status(result) mustBe INTERNAL_SERVER_ERROR
        getInternalServerError must startWith("Error finding submission relating to declaration with Id of")
      }

      "when findDeclaration method returns a None" in {
        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(None))

        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(None))

        val result = controller.viewDeclaration("some-id")(getRequest())
        status(result) mustBe INTERNAL_SERVER_ERROR
        getInternalServerError must startWith("Error finding submission relating to declaration with Id of")
      }

      "when findDeclaration method returns a declaration with no declarationMeta.associatedSubmissionId defined" in {
        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(Some(aDeclaration(withId("some-id"), withAssociatedSubmissionId(None)))))

        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(None))

        val result = controller.viewDeclaration("some-id")(getRequest())
        status(result) mustBe INTERNAL_SERVER_ERROR
        getInternalServerError must startWith("Error finding submission relating to declaration with Id of")
      }

      "when findSubmission method returns a failed future" in {
        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(Some(aDeclaration(withId("some-id"), withAssociatedSubmissionId(Some(submission.uuid))))))

        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.failed(throwable))

        val result = controller.viewDeclaration("some-id")(getRequest())
        status(result) mustBe INTERNAL_SERVER_ERROR
        getInternalServerError must startWith("Error finding submission relating to declaration with Id of")
      }

      "when findSubmission method returns a None" in {
        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(Some(aDeclaration(withId("some-id"), withAssociatedSubmissionId(Some(submission.uuid))))))

        when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
          .thenReturn(Future.successful(None))

        val result = controller.viewDeclaration("some-id")(getRequest())
        status(result) mustBe INTERNAL_SERVER_ERROR
        getInternalServerError must startWith("Failed to find submission relating to declaration with Id of")
      }
    }
  }

  def inTheLast(timeout: Timeout): BeMatcher[Instant] = new BeMatcher[Instant] {
    override def apply(left: Instant): MatchResult = {
      val currentTime = Instant.now()
      MatchResult(
        left != null && left.plusSeconds(timeout.duration.toSeconds).isAfter(currentTime),
        s"Instant was ${currentTime.getEpochSecond - left.getEpochSecond} seconds ago",
        s"Instant was ${currentTime.getEpochSecond - left.getEpochSecond} seconds ago, expected it to be later"
      )
    }
  }

  def theDeclarationCreated: ExportsDeclaration = {
    val captor: ArgumentCaptor[ExportsDeclaration] = ArgumentCaptor.forClass(classOf[ExportsDeclaration])
    verify(mockCustomsDeclareExportsConnector).createDeclaration(captor.capture(), any[String])(any(), any())
    captor.getValue
  }

  private val rejectedId = "id"
  private val declarationId = "declarationId"

  "SubmissionsController on amend" should {

    "return 303 (SEE OTHER) with the new declaration-id as one the Session keys on declaration submission" in {
      val isAmendment = false
      initMock(isAmendment)

      val result = controller.amend(rejectedId, isAmendment)(getRequest(None))
      verifyResult(result, SummaryController.displayPage.url)
    }

    "return 303 (SEE OTHER) with the new declaration-id as one the Session keys on declaration amendment" in {
      val isAmendment = true
      initMock(isAmendment)

      val result = controller.amend(rejectedId, isAmendment)(getRequest(None))
      verifyResult(result, SummaryController.displayPage.url)
    }
  }

  "SubmissionsController on amendErrors" should {

    "return 303 (SEE OTHER) with the new declaration-id as one the Session keys on declaration submission" in {
      val isAmendment = false
      initMock(isAmendment)

      val redirectUrl = "/specific-page-url"
      val result = controller.amendErrors(rejectedId, "pattern", "message", isAmendment, RedirectUrl(redirectUrl))(getRequest(None))

      verifyResult(result, redirectUrl)
    }

    "return 303 (SEE OTHER) with the new declaration-id as one the Session keys on declaration amendment" in {
      val isAmendment = true
      initMock(isAmendment)

      val redirectUrl = "/specific-page-url"
      val result = controller.amendErrors(rejectedId, "pattern", "message", isAmendment, RedirectUrl(redirectUrl))(getRequest(None))

      verifyResult(result, redirectUrl)
    }
  }

  private def initMock(isAmendment: Boolean): Unit = {
    withNewCaching(aDeclaration())

    if (isAmendment)
      when(mockCustomsDeclareExportsConnector.findOrCreateDraftForAmendment(refEq(rejectedId), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(declarationId))
    else
      when(mockCustomsDeclareExportsConnector.findOrCreateDraftForRejection(refEq(rejectedId), any(), any())(any(), any()))
        .thenReturn(Future.successful(declarationId))
  }

  private def verifyResult(result: Future[Result], expectedRedirect: String): Assertion = {
    status(result) mustBe SEE_OTHER
    redirectLocation(result).get mustBe expectedRedirect
    session(result).get(declarationUuid) mustBe Some(declarationId)
  }

  protected def getInternalServerError: String = {
    val callCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
    verify(mockErrorHandler).internalServerError(callCaptor.capture())(any())
    callCaptor.getValue
  }
}
