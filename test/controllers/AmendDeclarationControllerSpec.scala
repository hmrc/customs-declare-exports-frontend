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
import config.AppConfig
import config.featureFlags.DeclarationAmendmentsConfig
import controllers.declaration.routes.SummaryController
import controllers.routes.RootController
import handlers.ErrorHandler
import models.requests.ExportsSessionKeys
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.error_template

import scala.concurrent.Future

class AmendDeclarationControllerSpec extends ControllerWithoutFormSpec {

  val mcc = stubMessagesControllerComponents()
  val declarationAmendmentsConfig = mock[DeclarationAmendmentsConfig]

  val controller = new AmendDeclarationController(
    mockAuthAction,
    mockVerifiedEmailAction,
    new ErrorHandler(mcc.messagesApi, instanceOf[error_template])(instanceOf[AppConfig]),
    mcc,
    declarationAmendmentsConfig,
    mockCustomsDeclareExportsConnector
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(declarationAmendmentsConfig.isEnabled).thenReturn(true)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(declarationAmendmentsConfig, mockCustomsDeclareExportsConnector)
  }

  "AmendDeclarationController.submit" should {

    "redirect to /" when {
      "the amend flag is disabled" in {
        when(declarationAmendmentsConfig.isEnabled).thenReturn(false)

        val result = controller.initAmendment(FakeRequest("GET", ""))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(RootController.displayPage.url)
      }
    }

    "return 400(BAD_REQUEST)" when {
      "there is no 'submissionId' key in Session" in {
        val result = controller.initAmendment(FakeRequest("GET", ""))
        status(result) mustBe BAD_REQUEST
      }
    }

    "redirect to /saved-summary" when {
      "a declaration-id is returned by the connector" in {
        val expectedDeclarationId = "newDeclarationId"
        when(mockCustomsDeclareExportsConnector.findOrCreateDraftForAmend(any())(any(), any()))
          .thenReturn(Future.successful(expectedDeclarationId))

        val request = FakeRequest("GET", "").withSession(ExportsSessionKeys.submissionId -> "submissionId")
        val result = controller.initAmendment(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(SummaryController.displayPage.url)
        session(result).get(ExportsSessionKeys.declarationId) mustBe Some(expectedDeclarationId)
      }
    }
  }
}
