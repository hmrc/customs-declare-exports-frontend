/*
 * Copyright 2019 HM Revenue & Customs
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

import java.time.LocalDateTime
import java.util.UUID

import controllers.SubmissionsController
import models.declaration.notifications.Notification
import models.declaration.submissions.RequestType.SubmissionRequest
import models.declaration.submissions.{Action, Submission}
import models.requests.ExportsSessionKeys
import models.{DeclarationStatus, ExportsDeclaration, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.test.Helpers._
import unit.base.ControllerSpec
import views.html.submissions

import scala.concurrent.Future.successful

class SubmissionsControllerSpec extends ControllerSpec {

  private val notification = Notification("convId", "mrn", LocalDateTime.now(), "01", None, Seq.empty, "payload")
  private val submission = Submission(
    uuid = UUID.randomUUID().toString,
    eori = "eori",
    lrn = "lrn",
    mrn = None,
    ducr = None,
    actions = Seq(
      Action(requestType = SubmissionRequest, conversationId = "conversationID", requestTimestamp = LocalDateTime.now())
    )
  )

  trait SetUp {
    val submissionsPage = new submissions(mainTemplate)

    val controller = new SubmissionsController(
      mockAuthAction,
      mockCustomsDeclareExportsConnector,
      stubMessagesControllerComponents(),
      submissionsPage
    )(ec)

    authorizedUser()
  }

  "Display Submissions" should {
    "return 200 (OK)" when {
      "display page method is invoked" in new SetUp {
        when(mockCustomsDeclareExportsConnector.fetchSubmissions()(any(), any())).thenReturn(successful(Seq(submission)))
        when(mockCustomsDeclareExportsConnector.fetchNotifications()(any(), any())).thenReturn(successful(Seq(notification)))

        val result = controller.displayListOfSubmissions()(getRequest())

        status(result) must be(OK)
      }
    }
  }

  "Amend Submission" should {
    "return 303 (SEE OTHER)" when {
      "declaration found" in new SetUp {
        val rejectedDeclaration: ExportsDeclaration = aDeclaration(withId("id"), withStatus(DeclarationStatus.COMPLETE))
        val newDeclaration: ExportsDeclaration = aDeclaration(withId("new-id"), withStatus(DeclarationStatus.DRAFT))
        when(mockCustomsDeclareExportsConnector.findDeclaration(refEq("id"))(any(), any())).thenReturn(successful(Some(rejectedDeclaration)))
        when(mockCustomsDeclareExportsConnector.createDeclaration(any[ExportsDeclaration])(any(), any())).thenReturn(successful(newDeclaration))

        val result = controller.amend("id")(getRequest())

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.declaration.routes.SummaryController.displayPage(Mode.AmendMode).url))
        session(result).get(ExportsSessionKeys.declarationId) must be(Some("new-id"))
        theDeclarationCreated.status mustBe DeclarationStatus.DRAFT
      }

      "declaration not found" in new SetUp {
        when(mockCustomsDeclareExportsConnector.findDeclaration(refEq("id"))(any(), any())).thenReturn(successful(None))

        val result = controller.amend("id")(getRequest())

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.routes.SubmissionsController.displayListOfSubmissions().url))
      }

      def theDeclarationCreated: ExportsDeclaration = {
        val captor: ArgumentCaptor[ExportsDeclaration] = ArgumentCaptor.forClass(classOf[ExportsDeclaration])
        verify(mockCustomsDeclareExportsConnector).createDeclaration(captor.capture())(any(), any())
        captor.getValue
      }
    }
  }
}
