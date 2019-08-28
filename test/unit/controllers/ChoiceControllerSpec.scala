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

import controllers.ChoiceController
import forms.Choice
import forms.Choice.AllowedChoiceValues._
import models.requests.ExportsSessionKeys
import models.{DeclarationStatus, ExportsDeclaration}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.html.choice_page
import utils.FakeRequestCSRFSupport._

import scala.concurrent.Future

class ChoiceControllerSpec extends ControllerSpec with ErrorHandlerMocks {
  import ChoiceControllerSpec._

  private def existingDeclaration(choice: String = SupplementaryDec) =
    aDeclaration(withId("existingDeclarationId"), withChoice(choice))

  private val newDeclaration =
    aDeclaration(withId("newDeclarationId"), withChoice(SupplementaryDec))

  trait SetUp {
    val choicePage = new choice_page(mainTemplate, minimalAppConfig)
    val controller =
      new ChoiceController(mockAuthAction, mockExportsCacheService, stubMessagesControllerComponents(), choicePage)(ec)
    setupErrorHandler()
    authorizedUser()
  }

  def postChoiceRequest(body: JsValue): Request[AnyContentAsJson] =
    FakeRequest("POST", "")
      .withJsonBody(body)
      .withCSRFToken

  "Display" should {

    "return 200 (OK)" when {

      "display page method is invoked with empty cache" in new SetUp {
        when(mockExportsCacheService.get(any())(any())).thenReturn(Future.successful(None))

        val result = controller.displayPage()(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked with data in cache" in new SetUp {
        when(mockExportsCacheService.get(any())(any())).thenReturn(Future.successful(Some(existingDeclaration())))

        val result = controller.displayPage()(getRequest())

        status(result) must be(OK)
      }
    }
  }

  "Submit" should {

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in new SetUp {

        val result = controller.submitChoice()(postChoiceRequest(incorrectChoice))

        status(result) must be(BAD_REQUEST)
        verifyTheCacheIsUnchanged()
      }
    }

    "redirect to Dispatch Location page" when {

      "user chooses Supplementary Dec for new declaration" in new SetUp {
        when(mockExportsCacheService.create(any[ExportsDeclaration])(any()))
          .thenReturn(Future.successful(newDeclaration))

        val result = controller.submitChoice()(postChoiceRequest(supplementaryChoice))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(
          Some(controllers.declaration.routes.DispatchLocationController.displayPage().url)
        )
        session(result).get(ExportsSessionKeys.declarationId) must be(newDeclaration.id)
        val created: ExportsDeclaration = theCacheModelCreated
        created.id mustBe None
        created.status mustBe DeclarationStatus.DRAFT
        created.choice mustBe "SMP"
      }

      "user chooses Supplementary Dec for existing Standard Dec" in new SetUp {
        val existingDec = existingDeclaration(StandardDec)
        when(mockExportsCacheService.get(any())(any())).thenReturn(Future.successful(Some(existingDec)))
        when(mockExportsCacheService.update(any())(any()))
          .thenReturn(Future.successful(Some(existingDec)))

        val result = controller.submitChoice()(postRequest(supplementaryChoice, existingDec))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(
          Some(controllers.declaration.routes.DispatchLocationController.displayPage().url)
        )
        val updated: ExportsDeclaration = theCacheModelUpdated
        updated.id mustBe existingDec.id
        updated.choice mustBe "SMP"
      }

      "user chooses Standard Dec for new declaration" in new SetUp {
        when(mockExportsCacheService.create(any[ExportsDeclaration])(any()))
          .thenReturn(Future.successful(newDeclaration))

        val result = controller.submitChoice()(postChoiceRequest(standardChoice))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(
          Some(controllers.declaration.routes.DispatchLocationController.displayPage().url)
        )
        session(result).get(ExportsSessionKeys.declarationId) must be(newDeclaration.id)
        val created: ExportsDeclaration = theCacheModelCreated
        created.id mustBe None
        created.status mustBe DeclarationStatus.DRAFT
        created.choice mustBe "STD"
      }

      "user chooses Standard Dec for existing Supplementary Dec" in new SetUp {
        val existingDec = existingDeclaration(SupplementaryDec)
        when(mockExportsCacheService.get(any())(any())).thenReturn(Future.successful(Some(existingDec)))
        when(mockExportsCacheService.update(any())(any()))
          .thenReturn(Future.successful(Some(existingDec)))

        val result = controller.submitChoice()(postRequest(standardChoice, existingDec))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(
          Some(controllers.declaration.routes.DispatchLocationController.displayPage().url)
        )
        val updated: ExportsDeclaration = theCacheModelUpdated
        updated.id mustBe existingDec.id
        updated.choice mustBe "STD"
      }
    }

    "redirect to Cancel Declaration page" when {

      "user chose Cancel Dec" in new SetUp {

        val result = controller.submitChoice()(postChoiceRequest(cancelChoice))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.routes.CancelDeclarationController.displayForm().url))
        verifyTheCacheIsUnchanged()
      }
    }

    "redirect to Submissions page" when {

      "user chose submissions" in new SetUp {

        val result = controller.submitChoice()(postChoiceRequest(submissionsChoice))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.routes.SubmissionsController.displayListOfSubmissions().url))
        verifyTheCacheIsUnchanged()
      }
    }

    "redirect to Saved Declarations page" when {

      "user chose continue a saved declaration" in new SetUp {

        val result = controller.submitChoice()(postChoiceRequest(continueDeclarationChoice))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.routes.SavedDeclarationsController.displayDeclarations().url))
        verifyTheCacheIsUnchanged()
      }
    }
  }
}

object ChoiceControllerSpec {
  val incorrectChoice: JsValue = Json.toJson(Choice("Incorrect Choice"))
  val supplementaryChoice: JsValue = Json.toJson(Choice(SupplementaryDec))
  val standardChoice: JsValue = Json.toJson(Choice(StandardDec))
  val cancelChoice: JsValue = Json.toJson(Choice(CancelDec))
  val submissionsChoice: JsValue = Json.toJson(Choice(Submissions))
  val continueDeclarationChoice: JsValue = Json.toJson(Choice(ContinueDec))
}
