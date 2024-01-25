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
import controllers.declaration.routes.{AdditionalDeclarationTypeController, DeclarationChoiceController}
import forms.declaration.DeclarationChoice.{form, nonStandardJourneys, NonStandardDeclarationType}
import models.DeclarationType._
import models.ExportsDeclaration
import models.declaration.DeclarationStatus.AMENDMENT_DRAFT
import models.requests.SessionHelper
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import utils.FakeRequestCSRFSupport._
import views.html.declaration.standard_or_other_journey

class StandardOrOtherJourneyControllerSpec extends ControllerWithoutFormSpec with OptionValues {

  private val newDeclarationId = "newDeclarationId"

  private val standardOrOtherJourney = mock[standard_or_other_journey]
  private val mcc = stubMessagesControllerComponents()

  val controller =
    new StandardOrOtherJourneyController(mockAuthAction, mockVerifiedEmailAction, mockExportsCacheService, mcc, standardOrOtherJourney)(ec)

  override def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(standardOrOtherJourney.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(standardOrOtherJourney)
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

  "StandardOrOtherJourneyController.displayPage" should {

    "return 200 (OK)" when {

      "is invoked with empty cache" in {
        withNoDeclaration()

        val result = controller.displayPage(getRequest())
        status(result) must be(OK)
      }

      "is invoked with data in cache" in {
        withNewCaching(aDeclaration(withType(STANDARD)))

        val result = controller.displayPage(getRequest())
        status(result) must be(OK)
      }
    }

    "return 303 (SEE_OTHER)" when {
      "has 'AMENDMENT_DRAFT' status" in {
        withNewCaching(aDeclaration(withType(STANDARD), withStatus(AMENDMENT_DRAFT)))

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

        viewOf(result) must be(standardOrOtherJourney(form(nonStandardJourneys))(request, controller.messagesApi.preferred(request)))
      }
    }
  }

  "StandardOrOtherJourneyController.submitChoice" should {

    "return 400 (BAD_REQUEST)" when {
      "form is incorrect" in {
        withNoDeclaration()

        val result = controller.submitChoice(postChoiceRequest(type2Json("InvalidChoice")))

        status(result) must be(BAD_REQUEST)
        verifyTheCacheIsUnchanged()
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user selects the 'Other declaration type' radio" in {
        withNoDeclaration()

        val result = controller.submitChoice(postChoiceRequest(type2Json(NonStandardDeclarationType), None))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(DeclarationChoiceController.displayPage.url))
        verifyTheCacheIsUnchanged()
      }

      "user creates a new STANDARD declaration" in {
        withNoDeclaration()
        withCreateResponse(aDeclaration())

        val result = controller.submitChoice(postChoiceRequest(type2Json(STANDARD.toString), None))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(AdditionalDeclarationTypeController.displayPage.url))
        verify(mockExportsCacheService).create(any[ExportsDeclaration], any[String])(any())
      }

      "user confirms the type of an existing STANDARD declaration" in {
        withNewCaching(aDeclaration())

        val result = controller.submitChoice(postChoiceRequest(type2Json(STANDARD.toString)))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(AdditionalDeclarationTypeController.displayPage.url))
        verifyTheCacheIsUnchanged()
      }

      s"a STANDARD declaration is existing and has 'AMENDMENT_DRAFT' status (and not allow the user to change it)" in {
        withNewCaching(aDeclaration(withStatus(AMENDMENT_DRAFT)))

        val result = controller.submitChoice(postChoiceRequest(type2Json(NonStandardDeclarationType)))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(AdditionalDeclarationTypeController.displayPage.url))

        verifyTheCacheIsUnchanged()
      }
    }

    "sets the session declarationUuid to the target declaration being used" when {

      "user creates a new STANDARD declaration" in {
        withCreateResponse(aDeclaration(withId(newDeclarationId)))

        val result = controller.submitChoice(postChoiceRequest(type2Json(STANDARD.toString), None))

        session(result).get(SessionHelper.declarationUuid).value mustEqual newDeclarationId
      }

      "user confirm the type of an existing STANDARD declaration" in {
        withNewCaching(aDeclaration(withId(existingDeclarationId)))

        val result = controller.submitChoice()(postChoiceRequest(type2Json(STANDARD.toString)))

        session(result).get(SessionHelper.declarationUuid).value mustEqual existingDeclarationId
      }

      "user changes the type of an existing STANDARD declaration" in {
        withNewCaching(aDeclaration(withId(existingDeclarationId)))

        val result = controller.submitChoice()(postChoiceRequest(type2Json(NonStandardDeclarationType)))

        session(result).get(SessionHelper.declarationUuid).value mustEqual existingDeclarationId
      }
    }

    "NOT create a new declaration and NOT set the session's declarationUuid" when {
      "user selects the 'Other declaration type' radio" in {
        val result = controller.submitChoice(postChoiceRequest(type2Json(NonStandardDeclarationType), None))

        verifyTheCacheIsUnchanged()
        session(result).get(SessionHelper.declarationUuid) mustBe None
      }
    }

    def type2Json(declarationType: String): JsObject = Json.obj("type" -> declarationType)
  }
}
