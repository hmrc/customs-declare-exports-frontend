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

package controllers.declaration.amendments

import base.ControllerWithoutFormSpec
import controllers.declaration.routes.SummaryController
import controllers.routes.RootController
import models.declaration.submissions.EnhancedStatus.{EnhancedStatus, PENDING}
import models.requests.SessionHelper
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, when}
import org.scalatest.OptionValues
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testdata.SubmissionsTestData

import scala.concurrent.Future

class AmendDeclarationControllerSpec extends ControllerWithoutFormSpec with OptionValues {

  val controller = new AmendDeclarationController(
    mockAuthAction,
    mockVerifiedEmailAction,
    stubMessagesControllerComponents(),
    mockCustomsDeclareExportsConnector,
    mockDeclarationAmendmentsConfig
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(mockDeclarationAmendmentsConfig.isEnabled).thenReturn(true)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockDeclarationAmendmentsConfig, mockCustomsDeclareExportsConnector)
  }

  val request = FakeRequest()

  "AmendDeclarationController.initAmendment" should {

    "redirect to /" when {
      "the amend flag is disabled" in {
        when(mockDeclarationAmendmentsConfig.isEnabled).thenReturn(false)

        val result = controller.initAmendment("parentId", "QUERY_NOTIFICATION_MESSAGE")(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(RootController.displayPage.url)
      }
    }

    "redirect to /saved-summary" when {
      "a declaration-id is returned by the connector" in {
        val expectedDeclarationId = "newDeclarationId"
        when(mockCustomsDeclareExportsConnector.findOrCreateDraftForAmendment(anyString(), any[EnhancedStatus])(any(), any()))
          .thenReturn(Future.successful(expectedDeclarationId))

        val result = controller.initAmendment("parentId", "PENDING")(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(SummaryController.displayPage.url)
        session(result).get(SessionHelper.declarationUuid) mustBe Some(expectedDeclarationId)
      }
    }

    "return the expected Call" in {
      val submission = SubmissionsTestData.submission.copy(latestEnhancedStatus = Some(PENDING))
      val expectedStatus = submission.latestEnhancedStatus.value.toString
      val expectedRoute = routes.AmendDeclarationController.initAmendment(submission.latestDecId.value, expectedStatus)
      AmendDeclarationController.initAmendment(submission) mustBe expectedRoute
    }
  }
}
