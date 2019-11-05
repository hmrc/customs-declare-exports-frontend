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

package unit.controllers.declaration

import controllers.declaration.DeclarationChoiceController
import forms.Choice
import forms.declaration.DeclarationChoiceSpec
import models.DeclarationType.DeclarationType
import models.requests.ExportsSessionKeys
import models.{DeclarationStatus, DeclarationType}
import org.scalatest.OptionValues
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContentAsJson, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import unit.base.ControllerSpec
import utils.FakeRequestCSRFSupport._
import views.html.declaration.declaration_choice

class DeclarationChoiceControllerSpec extends ControllerSpec with OptionValues {
  import DeclarationChoiceSpec._

  private def existingDeclaration(choice: DeclarationType = DeclarationType.SUPPLEMENTARY) =
    aDeclaration(withId("existingDeclarationId"), withType(choice))

  private val newDeclaration =
    aDeclaration(withId("newDeclarationId"), withType(DeclarationType.SUPPLEMENTARY))

  val choicePage = new declaration_choice(mainTemplate, minimalAppConfig)

  val controller =
    new DeclarationChoiceController(mockAuthAction, mockExportsCacheService, stubMessagesControllerComponents(), choicePage)(ec)

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

        val result = controller.displayPage(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked with data in cache" in {
        withNewCaching(existingDeclaration())

        val result = controller.displayPage(getRequest())

        status(result) must be(OK)
      }
    }

    "not select any choice " when {

      "cache empty" in {
        withNoDeclaration()

        val request = getRequest()
        val result = controller.displayPage(request)
        var form = Choice.form()

        viewOf(result) must be(choicePage(form)(request, controller.messagesApi.preferred(request)))
      }
    }
  }

  "Submit" should {

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in {

        val result = controller.submitChoice()(postChoiceRequest(incorrectChoiceJSON))

        status(result) must be(BAD_REQUEST)
        verifyTheCacheIsUnchanged()
      }
    }

    "redirect" when {

      "user chooses Standard Dec for new declaration" in {
        withCreateResponse(newDeclaration)

        val result = controller.submitChoice()(postChoiceRequest(correctChoiceJSON))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.declaration.routes.DispatchLocationController.displayPage().url))
        session(result).get(ExportsSessionKeys.declarationId).value mustEqual newDeclaration.id
        val created = theCacheModelCreated
        created.id mustBe None
        created.status mustBe DeclarationStatus.DRAFT
        created.`type` mustBe DeclarationType.STANDARD
      }
    }

  }

}
