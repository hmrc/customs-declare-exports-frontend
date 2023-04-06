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

import akka.util.Timeout
import base.ControllerWithoutFormSpec
import config.PaginationConfig
import controllers.declaration.routes._
import models._
import models.declaration.submissions.EnhancedStatus.GOODS_ARRIVED
import models.declaration.submissions.RequestType.SubmissionRequest
import models.declaration.submissions.{Action, Submission}
import models.requests.SessionHelper
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.{BeMatcher, MatchResult}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.dashboard.DashboardHelper.toDashboard
import views.html.declaration.summary.submitted_declaration_page

import java.time.{Instant, ZoneOffset, ZonedDateTime}
import java.util.UUID
import scala.concurrent.Future

class SubmissionsControllerSpec extends ControllerWithoutFormSpec with BeforeAndAfterEach {

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
    mockCustomsDeclareExportsConnector,
    stubMessagesControllerComponents(),
    submittedDeclarationPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(submittedDeclarationPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(paginationConfig.itemsPerPage).thenReturn(Page.DEFAULT_MAX_DOCUMENT_PER_PAGE)
  }

  override protected def afterEach(): Unit =
    reset(submittedDeclarationPage, mockCustomsDeclareExportsConnector, paginationConfig)

  "SubmissionsController on viewDeclaration" should {

    "return 200 (OK)" when {
      "there is a Declaration with given ID in the cache" which {

        "has related Notifications" in {
          when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
            .thenReturn(Future.successful(Some(aDeclaration(withId("some-id")))))

          when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
            .thenReturn(Future.successful(Some(submission)))

          val result = controller.viewDeclaration("some-id")(getRequest())

          status(result) mustBe OK
        }

        "has NO related Notifications" in {
          when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
            .thenReturn(Future.successful(Some(aDeclaration(withId("some-id")))))

          when(mockCustomsDeclareExportsConnector.findSubmission(any())(any(), any()))
            .thenReturn(Future.successful(None))

          val result = controller.viewDeclaration("some-id")(getRequest())

          status(result) mustBe OK
        }
      }
    }

    "return 303 (SEE_OTHER)" when {
      "there is no Declaration with given ID in the cache" in {
        when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any())).thenReturn(Future.successful(None))
        when(mockCustomsDeclareExportsConnector.findNotifications(any())(any(), any())).thenReturn(Future.successful(Seq.empty))

        val result = controller.viewDeclaration("some-id")(getRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe toDashboard.url
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
    verify(mockCustomsDeclareExportsConnector).createDeclaration(captor.capture())(any(), any())
    captor.getValue
  }

  private val rejectedId = "id"

  "SubmissionsController on amend" should {
    "return 303 (SEE OTHER) with the new declaration-id as one the Session keys" in {
      when(mockCustomsDeclareExportsConnector.findOrCreateDraftForRejected(refEq(rejectedId))(any(), any()))
        .thenReturn(Future.successful("new-id"))

      val result = controller.amend(rejectedId)(getRequest(None))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(SummaryController.displayPage.url)
      session(result).get(SessionHelper.declarationUuid) mustBe Some("new-id")
    }
  }

  "SubmissionsController on amendErrors" should {
    "return 303 (SEE OTHER) with the new declaration-id as one the Session keys" in {
      when(mockCustomsDeclareExportsConnector.findOrCreateDraftForRejected(refEq(rejectedId))(any(), any()))
        .thenReturn(Future.successful("new-id"))

      val redirectUrl = "/specific-page-url"
      val result = controller.amendErrors(rejectedId, redirectUrl, "pattern", "message")(getRequest(None))

      status(result) mustBe SEE_OTHER
      redirectLocation(result).get mustBe redirectUrl
      session(result).get(SessionHelper.declarationUuid) mustBe Some("new-id")
    }
  }
}
