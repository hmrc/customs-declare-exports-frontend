/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.{Instant, LocalDate, LocalDateTime}
import java.util.UUID

import akka.util.Timeout
import base.Injector
import connectors.exchange.ExportsDeclarationExchange
import controllers.SubmissionsController
import models.declaration.notifications.Notification
import models.declaration.submissions.RequestType.SubmissionRequest
import models.declaration.submissions.{Action, Submission, SubmissionStatus}
import models.requests.ExportsSessionKeys
import models.{DeclarationStatus, ExportsDeclaration, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.{BeMatcher, MatchResult}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.{declaration_information, submissions}

import scala.concurrent.Future.successful
import scala.concurrent.duration._

class SubmissionsControllerSpec extends ControllerSpec with BeforeAndAfterEach with Injector {

  private val notification =
    Notification("actionId", "mrn", LocalDateTime.now(), SubmissionStatus.UNKNOWN, Seq.empty, "payload")
  private val submission = Submission(
    uuid = UUID.randomUUID().toString,
    eori = "eori",
    lrn = "lrn",
    mrn = None,
    ducr = None,
    actions = Seq(Action(requestType = SubmissionRequest, id = "conversationID", requestTimestamp = LocalDateTime.now()))
  )
  val declarationInformationPage = mock[declaration_information]

  override protected def beforeEach(): Unit =
    when(declarationInformationPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)

  override protected def afterEach(): Unit =
    reset(declarationInformationPage)

  trait SetUp {
    val submissionsPage = instanceOf[submissions]
    val controller =
      new SubmissionsController(
        mockAuthAction,
        mockCustomsDeclareExportsConnector,
        stubMessagesControllerComponents(),
        submissionsPage,
        declarationInformationPage
      )(ec)

    authorizedUser()
  }

  "Display Submissions" should {
    "return 200 (OK)" when {
      "display page method is invoked" in new SetUp {
        when(mockCustomsDeclareExportsConnector.fetchSubmissions()(any(), any()))
          .thenReturn(successful(Seq(submission)))
        when(mockCustomsDeclareExportsConnector.fetchNotifications()(any(), any()))
          .thenReturn(successful(Seq(notification)))

        val result = controller.displayListOfSubmissions()(getRequest())

        status(result) must be(OK)
      }
    }
  }

  "Amend Submission" should {
    "return 303 (SEE OTHER)" when {
      "declaration found" in new SetUp {
        val rejectedDeclaration: ExportsDeclaration =
          aDeclaration(withId("id"), withStatus(DeclarationStatus.COMPLETE), withUpdateDate(LocalDate.MIN), withCreatedDate(LocalDate.MIN))
        val newDeclaration: ExportsDeclaration = aDeclaration(withId("new-id"), withStatus(DeclarationStatus.DRAFT))
        when(mockCustomsDeclareExportsConnector.findDeclaration(refEq("id"))(any(), any()))
          .thenReturn(successful(Some(rejectedDeclaration)))
        when(mockCustomsDeclareExportsConnector.createDeclaration(any[ExportsDeclarationExchange])(any(), any()))
          .thenReturn(successful(newDeclaration))

        val result = controller.amend("id")(getRequest())

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.declaration.routes.SummaryController.displayPage(Mode.Amend).url))
        session(result).get(ExportsSessionKeys.declarationId) must be(Some("new-id"))
        val created = theDeclarationCreated
        created.status mustBe DeclarationStatus.DRAFT
        created.sourceId mustBe Some("id")
        created.id mustBe None
        created.updatedDateTime mustBe inTheLast(1 seconds)
        created.createdDateTime mustBe inTheLast(1 seconds)
      }

      "declaration not found" in new SetUp {
        when(mockCustomsDeclareExportsConnector.findDeclaration(refEq("id"))(any(), any())).thenReturn(successful(None))

        val result = controller.amend("id")(getRequest())

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.routes.SubmissionsController.displayListOfSubmissions().url))
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

      def theDeclarationCreated: ExportsDeclarationExchange = {
        val captor: ArgumentCaptor[ExportsDeclarationExchange] = ArgumentCaptor.forClass(classOf[ExportsDeclarationExchange])
        verify(mockCustomsDeclareExportsConnector).createDeclaration(captor.capture())(any(), any())
        captor.getValue
      }
    }
  }
}
