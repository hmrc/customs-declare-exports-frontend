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

package unit.controllers.declaration

import java.time.{ZoneOffset, ZonedDateTime}

import controllers.declaration.AcceptedDeclarationController
import models.declaration.notifications.Notification
import models.declaration.submissions.SubmissionStatus
import models.declaration.submissions.SubmissionStatus.SubmissionStatus
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerWithoutFormSpec
import unit.mock.ErrorHandlerMocks
import views.html.declaration.summary.accepted_declaration_page

import scala.concurrent.Future

class AcceptedDeclarationControllerSpec extends ControllerWithoutFormSpec with ErrorHandlerMocks with OptionValues {

  private val acceptedDeclarationPage = mock[accepted_declaration_page]

  private val controller = new AcceptedDeclarationController(
    mockAuthAction,
    mockJourneyAction,
    mockCustomsDeclareExportsConnector,
    stubMessagesControllerComponents(),
    acceptedDeclarationPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(acceptedDeclarationPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(acceptedDeclarationPage)
    super.afterEach()
  }

  private def notification(status: SubmissionStatus) =
    Notification("actionId", "mrn", ZonedDateTime.now(ZoneOffset.UTC), status, Seq.empty, "payload")

  "Display" should {

    "return 200 (OK)" when {

      "declaration has been accepted" in {
        when(mockCustomsDeclareExportsConnector.findNotifications(any())(any(), any()))
          .thenReturn(Future.successful(Seq(notification(SubmissionStatus.ACCEPTED))))
        withNewCaching(aDeclaration())

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        verify(acceptedDeclarationPage, times(1)).apply(any())(any(), any())
      }

    }

    "return 303 (SEE_OTHER)" when {

      "declaration has not been accepted" in {
        when(mockCustomsDeclareExportsConnector.findNotifications(any())(any(), any()))
          .thenReturn(Future.successful(Seq(notification(SubmissionStatus.REJECTED))))
        withNewCaching(aDeclaration())

        val result = controller.displayPage()(getRequest())

        status(result) must be(SEE_OTHER)
        verify(acceptedDeclarationPage, times(0)).apply(any())(any(), any())
      }

    }
  }

}
