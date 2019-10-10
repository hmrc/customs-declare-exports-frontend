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
import org.scalatest.OptionValues
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import unit.base.ControllerSpec
import utils.FakeRequestCSRFSupport._
import views.html.choice_page

class ChoiceControllerSpec extends ControllerSpec with OptionValues {
  import ChoiceControllerSpec._

  private def existingDeclaration(choice: String = SupplementaryDec) =
    aDeclaration(withId("existingDeclarationId"), withChoice(choice))

  private val newDeclaration =
    aDeclaration(withId("newDeclarationId"), withChoice(SupplementaryDec))

  val choicePage = new choice_page(mainTemplate, minimalAppConfig)

  val controller =
    new ChoiceController(mockAuthAction, mockExportsCacheService, stubMessagesControllerComponents(), choicePage)(ec)

  override def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
  }

  def postChoiceRequest(body: JsValue): Request[AnyContentAsJson] =
    FakeRequest("POST", "")
      .withJsonBody(body)
      .withCSRFToken

  "Display" should {

    "return 200 (OK)" when {

      "display page method is invoked with empty cache" in {
        withNoDeclaration()

        val result = controller.displayPage(None)(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked with data in cache" in {
        withNewCaching(existingDeclaration())

        val result = controller.displayPage(None)(getRequest())

        status(result) must be(OK)
      }
    }

    "pre-select given choice " when {

      "cache is empty" in {
        withNoDeclaration()

        val request = getRequest()
        val result = controller.displayPage(Some(Choice(CancelDec)))(request)
        val form = Choice.form().fill(Choice(CancelDec))

        viewOf(result) must be(choicePage(form)(request, controller.messagesApi.preferred(request)))
      }

      "cache contains existing declaration" in {
        withNewCaching(existingDeclaration())

        val request = getRequest()
        val result = controller.displayPage(Some(Choice(Submissions)))(request)
        val form = Choice.form().fill(Choice(Submissions))

        viewOf(result) must be(choicePage(form)(request, controller.messagesApi.preferred(request)))
      }
    }

    "pre-select declaration type " when {

      "choice parameter not given" in {
        withNewCaching(existingDeclaration(SupplementaryDec))

        val request = getRequest()
        val result = controller.displayPage(None)(request)
        var form = Choice.form().fill(Choice(SupplementaryDec))

        viewOf(result) must be(choicePage(form)(request, controller.messagesApi.preferred(request)))
      }
    }

    "not select any choice " when {

      "choice parameter not given and cache empty" in {
        withNoDeclaration()

        val request = getRequest()
        val result = controller.displayPage(None)(request)
        var form = Choice.form()

        viewOf(result) must be(choicePage(form)(request, controller.messagesApi.preferred(request)))
      }
    }
  }

  "Submit" should {

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in {

        val result = controller.submitChoice()(postChoiceRequest(incorrectChoice))

        status(result) must be(BAD_REQUEST)
        verifyTheCacheIsUnchanged()
      }
    }

    "redirect to Dispatch Location page" when {

      "user chooses Supplementary Dec for new declaration" in {
        withCreateResponse(newDeclaration)

        val result = controller.submitChoice()(postChoiceRequest(supplementaryChoice))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(
          Some(controllers.declaration.routes.DispatchLocationController.displayPage().url)
        )
        session(result).get(ExportsSessionKeys.declarationId).value mustEqual newDeclaration.id
        val created = theCacheModelCreated
        created.id mustBe None
        created.status mustBe DeclarationStatus.DRAFT
        created.choice mustBe "SMP"
        created.sourceId mustBe None
      }

      "user chooses Supplementary Dec for existing Standard Dec" in {
        val existingDec = existingDeclaration(StandardDec)
        withNewCaching(existingDec)

        val result = controller.submitChoice()(postRequest(supplementaryChoice, existingDec))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(
          Some(controllers.declaration.routes.DispatchLocationController.displayPage().url)
        )
        val updated: ExportsDeclaration = theCacheModelUpdated
        updated.id mustBe existingDec.id
        updated.choice mustBe "SMP"
      }

      "user chooses Standard Dec for new declaration" in {
        withCreateResponse(newDeclaration)

        val result = controller.submitChoice()(postChoiceRequest(standardChoice))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(
          Some(controllers.declaration.routes.DispatchLocationController.displayPage().url)
        )
        session(result).get(ExportsSessionKeys.declarationId).value mustEqual newDeclaration.id
        val created = theCacheModelCreated
        created.id mustBe None
        created.status mustBe DeclarationStatus.DRAFT
        created.choice mustBe "STD"
      }

      "user chooses Standard Dec for existing Supplementary Dec" in {
        val existingDec = existingDeclaration(SupplementaryDec)
        withNewCaching(existingDec)

        val result = controller.submitChoice()(postRequest(standardChoice, existingDec))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(
          Some(controllers.declaration.routes.DispatchLocationController.displayPage().url)
        )
        val updated = theCacheModelUpdated
        updated.id mustBe existingDec.id
        updated.choice mustBe "STD"
      }
    }

    "redirect to Cancel Declaration page" when {

      "user chose Cancel Dec" in {

        val result = controller.submitChoice()(postChoiceRequest(cancelChoice))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.routes.CancelDeclarationController.displayPage().url))
        verifyTheCacheIsUnchanged()
      }
    }

    "redirect to Submissions page" when {

      "user chose submissions" in {

        val result = controller.submitChoice()(postChoiceRequest(submissionsChoice))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.routes.SubmissionsController.displayListOfSubmissions().url))
        verifyTheCacheIsUnchanged()
      }
    }

    "redirect to Saved Declarations page" when {

      "user chose continue a saved declaration" in {

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
