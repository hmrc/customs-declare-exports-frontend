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
import config.PaginationConfig
import models._
import models.declaration.submissions.EnhancedStatus.GOODS_ARRIVED
import models.declaration.submissions.StatusGroup.SubmittedStatuses
import models.declaration.submissions.{Submission, SubmissionAction}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.dashboard.dashboard

import java.time.{ZoneOffset, ZonedDateTime}
import java.util.UUID
import scala.concurrent.Future
class DashboardControllerSpec extends ControllerWithoutFormSpec with BeforeAndAfterEach {

  val dateTime = ZonedDateTime.now(ZoneOffset.UTC)

  val uuid = UUID.randomUUID.toString

  private val action = SubmissionAction(id = "conversationID", requestTimestamp = dateTime, notifications = None, uuid)

  private val pageOfSubmissions = PageOfSubmissions(
    SubmittedStatuses,
    1,
    Seq(
      Submission(
        uuid = uuid,
        eori = "eori",
        lrn = "lrn",
        mrn = None,
        ducr = None,
        latestEnhancedStatus = Some(GOODS_ARRIVED),
        enhancedStatusLastUpdated = Some(dateTime),
        actions = Seq(action)
      )
    )
  )

  private val dashboard = mock[dashboard]
  private val paginationConfig = mock[PaginationConfig]

  val controller = new DashboardController(
    mockAuthAction,
    mockVerifiedEmailAction,
    mockCustomsDeclareExportsConnector,
    paginationConfig,
    stubMessagesControllerComponents(),
    dashboard
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(dashboard.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(paginationConfig.itemsPerPage).thenReturn(Page.DEFAULT_MAX_DOCUMENT_PER_PAGE)
  }

  override protected def afterEach(): Unit = reset(mockCustomsDeclareExportsConnector, dashboard, paginationConfig)

  def pageOfSubmissionsCaptor: PageOfSubmissions = {
    val captor = ArgumentCaptor.forClass(classOf[PageOfSubmissions])
    verify(dashboard).apply(captor.capture())(any(), any())
    captor.getValue
  }

  "DashboardController.displayPage" should {
    "return 200 (OK)" when {
      "display list of submissions method is invoked" in {
        when(mockCustomsDeclareExportsConnector.fetchSubmissionPage(any())(any(), any())).thenReturn(Future.successful(pageOfSubmissions))

        val result = controller.displayPage(getRequest())

        status(result) mustBe OK
        pageOfSubmissionsCaptor mustBe pageOfSubmissions
      }
    }
  }
}
