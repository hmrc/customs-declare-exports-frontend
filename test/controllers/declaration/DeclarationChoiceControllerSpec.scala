/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.declaration

import base.ControllerWithoutFormSpec
import controllers.declaration.routes.AdditionalDeclarationTypeController
import forms.Choice
import forms.declaration.AuthorisationProcedureCodeChoice.Choice1040
import forms.declaration.DeclarationChoiceSpec
import models.DeclarationType
import models.DeclarationType.{DeclarationType, _}
import models.requests.ExportsSessionKeys
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.OptionValues
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContentAsJson, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import utils.FakeRequestCSRFSupport._
import views.html.declaration.declaration_choice

class DeclarationChoiceControllerSpec extends ControllerWithoutFormSpec with OptionValues {
  import DeclarationChoiceSpec._

  private val newDeclarationId = "newDeclarationId"
  private def existingDeclaration(choice: DeclarationType = DeclarationType.SUPPLEMENTARY) =
    aDeclaration(withId("existingDeclarationId"), withType(choice))

  private val newDeclaration =
    aDeclaration(withId(newDeclarationId), withType(DeclarationType.SUPPLEMENTARY))

  val choicePage = mock[declaration_choice]

  val controller =
    new DeclarationChoiceController(mockAuthAction, mockVerifiedEmailAction, mockExportsCacheService, stubMessagesControllerComponents(), choicePage)(
      ec
    )

  override def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(choicePage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(choicePage)
    super.afterEach()
  }

  def postChoiceRequest(body: JsValue, decSessionId: Option[String] = None): Request[AnyContentAsJson] = {
    val fakeRequest = decSessionId.map { id =>
      FakeRequest("POST", "").withSession((ExportsSessionKeys.declarationId, id))
    }.getOrElse(FakeRequest("POST", ""))

    fakeRequest
      .withJsonBody(body)
      .withCSRFToken
  }

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
        val form = Choice.form

        viewOf(result) must be(choicePage(form)(request, controller.messagesApi.preferred(request)))
      }
    }
  }

  "calling submit method" should {

    "return 400 (BAD_REQUEST)" when {
      "form is incorrect" in {
        val result = controller.submitChoice()(postChoiceRequest(incorrectChoiceJSON))

        status(result) must be(BAD_REQUEST)
        verifyTheCacheIsUnchanged()
      }
    }

    "return 303 (SEE_OTHER)" when {
      DeclarationType.values.foreach { journeyType =>
        s"user creates a new $journeyType declaration" in {
          withCreateResponse(aDeclaration(withId(newDeclarationId), withType(journeyType)))

          val result = controller.submitChoice()(postChoiceRequest(createChoiceJSON(journeyType.toString)))

          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(AdditionalDeclarationTypeController.displayPage.url))
        }

        s"user updates an existing $journeyType declaration" in {
          val dec = aDeclaration(withId(existingDeclarationId), withType(journeyType))
          withNewCaching(aDeclarationAfter(dec, withAuthorisationProcedureCodeChoice(Choice1040)))

          val result = controller.submitChoice()(postChoiceRequest(createChoiceJSON(journeyType.toString), Some(existingDeclarationId)))

          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(AdditionalDeclarationTypeController.displayPage.url))
        }
      }
    }

    "sets session declarationId to the target declaration being used" when {
      DeclarationType.values.foreach { journeyType =>
        s"user creates a new $journeyType declaration" in {
          withCreateResponse(aDeclaration(withId(newDeclarationId), withType(journeyType)))

          val result = controller.submitChoice()(postChoiceRequest(createChoiceJSON(journeyType.toString)))

          session(result).get(ExportsSessionKeys.declarationId).value mustEqual newDeclaration.id
        }

        s"user updates an existing $journeyType declaration" in {
          val dec = aDeclaration(withId(existingDeclarationId), withType(journeyType))
          withNewCaching(aDeclarationAfter(dec, withAuthorisationProcedureCodeChoice(Choice1040)))

          val result = controller.submitChoice()(postChoiceRequest(createChoiceJSON(journeyType.toString), Some(existingDeclarationId)))

          session(result).get(ExportsSessionKeys.declarationId).value mustEqual existingDeclarationId
        }
      }
    }

    "update an existing declaration's journeyType and keep the current authorisationProcedureCodeChoice field value" when {
      Seq(STANDARD, SUPPLEMENTARY, OCCASIONAL).foreach { journeyType =>
        s"user updates an existing SIMPLIFIED declaration's journeyType to $journeyType" in {
          val dec = aDeclaration(withId(existingDeclarationId), withType(SIMPLIFIED))
          withNewCaching(aDeclarationAfter(dec, withAuthorisationProcedureCodeChoice(Choice1040)))

          val result = controller.submitChoice()(postChoiceRequest(createChoiceJSON(journeyType.toString), Some(existingDeclarationId)))
          status(result) must be(SEE_OTHER)

          val updatedDec = theCacheModelUpdated
          updatedDec.`type` must be(journeyType)
          updatedDec.parties.authorisationProcedureCodeChoice mustBe Choice1040
        }
      }
    }

    "update an existing declaration's journeyType and clear the current authorisationProcedureCodeChoice field value" when {
      Seq(CLEARANCE, SIMPLIFIED).foreach { journeyType =>
        s"user updates an existing STANDARD declaration's journeyType to $journeyType" in {
          val dec = aDeclaration(withId(existingDeclarationId), withType(STANDARD))
          withNewCaching(aDeclarationAfter(dec, withAuthorisationProcedureCodeChoice(Choice1040)))

          val result = controller.submitChoice()(postChoiceRequest(createChoiceJSON(journeyType.toString), Some(existingDeclarationId)))
          status(result) must be(SEE_OTHER)

          val updatedDec = theCacheModelUpdated
          updatedDec.`type` must be(journeyType)
          updatedDec.parties.authorisationProcedureCodeChoice mustBe None
        }
      }
    }
  }
}
