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

package controllers.declaration

import base.ControllerWithoutFormSpec
import controllers.declaration.routes.AdditionalDeclarationTypeController
import forms.declaration.AuthorisationProcedureCodeChoice.Choice1040
import forms.declaration.{DeclarationChoice, DeclarationChoiceSpec}
import models.DeclarationType._
import models.ExportsDeclaration
import models.declaration.DeclarationStatus
import models.requests.SessionHelper
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import utils.FakeRequestCSRFSupport._
import views.html.declaration.declaration_choice

class DeclarationChoiceControllerSpec extends ControllerWithoutFormSpec with OptionValues {
  import DeclarationChoiceSpec._

  private val newDeclarationId = "newDeclarationId"

  val declarationChoice = mock[declaration_choice]
  val mcc = stubMessagesControllerComponents()

  val controller = new DeclarationChoiceController(mockAuthAction, mockVerifiedEmailAction, mockExportsCacheService, mcc, declarationChoice)(ec)

  override def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(declarationChoice.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(declarationChoice)
    super.afterEach()
  }

  def postChoiceRequest(body: JsValue, maybeDeclarationId: Option[String] = Some(existingDeclarationId)): Request[AnyContentAsJson] = {
    val fakeRequest = maybeDeclarationId.map { declarationId =>
      FakeRequest("POST", "").withSession((SessionHelper.declarationUuid, declarationId))
    }.getOrElse(FakeRequest("POST", ""))

    fakeRequest
      .withJsonBody(body)
      .withCSRFToken
  }

  "DeclarationChoiceController.displayPage" should {

    "return 200 (OK)" when {

      "is invoked with empty cache" in {
        withNoDeclaration()

        val result = controller.displayPage(getRequest())
        status(result) must be(OK)
      }

      "is invoked with data in cache" in {
        withNewCaching(aDeclaration())

        val result = controller.displayPage(getRequest())
        status(result) must be(OK)
      }
    }

    "return 303 (SEE_OTHER)" when {
      "has 'AMENDMENT_DRAFT' status" in {
        withNewCaching(aDeclaration(withStatus(DeclarationStatus.AMENDMENT_DRAFT)))

        val result = controller.displayPage(getRequest())

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(AdditionalDeclarationTypeController.displayPage.url))
      }
    }

    "not select any choice" when {
      "the cache is empty" in {
        withNoDeclaration()

        val request = getRequest()
        val result = controller.displayPage(request)

        viewOf(result) must be(declarationChoice(DeclarationChoice.form)(request, controller.messagesApi.preferred(request)))
      }
    }
  }

  "DeclarationChoiceController.submitChoice" should {

    "return 400 (BAD_REQUEST)" when {
      "form is incorrect" in {
        withNoDeclaration()

        val result = controller.submitChoice(postChoiceRequest(incorrectChoiceJSON))

        status(result) must be(BAD_REQUEST)
        verifyTheCacheIsUnchanged()
      }
    }

    "return 303 (SEE_OTHER)" when {

      allDeclarationTypes.foreach { journeyType =>
        s"user creates a new $journeyType declaration" in {
          withNoDeclaration()
          withCreateResponse(aDeclaration())

          val result = controller.submitChoice(postChoiceRequest(createChoiceJSON(journeyType.toString), None))

          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(AdditionalDeclarationTypeController.displayPage.url))
          verify(mockExportsCacheService).create(any[ExportsDeclaration])(any())
        }

        s"user updates an existing $journeyType declaration" in {
          withNewCaching(aDeclaration(withType(journeyType)))

          val result = controller.submitChoice(postChoiceRequest(createChoiceJSON(journeyType.toString)))

          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(AdditionalDeclarationTypeController.displayPage.url))
          verify(mockExportsCacheService).update(any[ExportsDeclaration])(any())
        }

        s"a $journeyType declaration is existing and has 'AMENDMENT_DRAFT' status (and not allow the user to change it)" in {
          withNewCaching(aDeclaration(withType(journeyType), withStatus(DeclarationStatus.AMENDMENT_DRAFT)))

          val result = controller.submitChoice(postChoiceRequest(Json.obj()))

          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(AdditionalDeclarationTypeController.displayPage.url))

          verifyTheCacheIsUnchanged()
        }
      }
    }

    "sets session declarationUuid to the target declaration being used" when {
      allDeclarationTypes.foreach { journeyType =>
        s"user creates a new $journeyType declaration" in {
          withCreateResponse(aDeclaration(withId(newDeclarationId), withType(journeyType)))

          val result = controller.submitChoice(postChoiceRequest(createChoiceJSON(journeyType.toString), None))

          session(result).get(SessionHelper.declarationUuid).value mustEqual newDeclarationId
        }

        s"user updates an existing $journeyType declaration" in {
          val dec = aDeclaration(withId(existingDeclarationId), withType(journeyType))
          withNewCaching(aDeclarationAfter(dec, withAuthorisationProcedureCodeChoice(Choice1040)))

          val result = controller.submitChoice()(postChoiceRequest(createChoiceJSON(journeyType.toString)))

          session(result).get(SessionHelper.declarationUuid).value mustEqual existingDeclarationId
        }
      }
    }

    "update an existing declaration's journeyType and keep the current authorisationProcedureCodeChoice field value" when {
      Seq(STANDARD, SUPPLEMENTARY, OCCASIONAL).foreach { journeyType =>
        s"user updates an existing SIMPLIFIED declaration's journeyType to $journeyType" in {
          val dec = aDeclaration(withId(existingDeclarationId), withType(SIMPLIFIED))
          withNewCaching(aDeclarationAfter(dec, withAuthorisationProcedureCodeChoice(Choice1040)))

          val result = controller.submitChoice(postChoiceRequest(createChoiceJSON(journeyType.toString)))
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

          val result = controller.submitChoice(postChoiceRequest(createChoiceJSON(journeyType.toString)))
          status(result) must be(SEE_OTHER)

          val updatedDec = theCacheModelUpdated
          updatedDec.`type` must be(journeyType)
          updatedDec.parties.authorisationProcedureCodeChoice mustBe None
        }
      }
    }
  }
}
