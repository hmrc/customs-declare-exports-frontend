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

package controllers

import base.ExportsTestData._
import base.{ControllerWithoutFormSpec, Injector}
import config.{AppConfig, ExternalServicesConfig}
import controllers.declaration.routes.DeclarationChoiceController
import forms.Choice
import forms.Choice.AllowedChoiceValues._
import models.DeclarationType
import models.DeclarationType.DeclarationType
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.OptionValues
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import utils.FakeRequestCSRFSupport._
import views.dashboard.DashboardHelper.toDashboard
import views.html.choice_page

class ChoiceControllerSpec extends ControllerWithoutFormSpec with OptionValues with Injector {
  import ChoiceControllerSpec._

  val choicePage = mock[choice_page]
  override val appConfig = mock[AppConfig]
  val externalServicesConfig = instanceOf[ExternalServicesConfig]

  val controller =
    new ChoiceController(
      mockAuthAction,
      mockVerifiedEmailAction,
      stubMessagesControllerComponents(),
      mockSecureMessagingInboxConfig,
      choicePage,
      appConfig,
      externalServicesConfig
    )

  override def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(choicePage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(appConfig.availableJourneys()).thenReturn(allJourneys)
  }

  override protected def afterEach(): Unit = {
    reset(choicePage, appConfig, mockSecureMessagingInboxConfig)
    super.afterEach()
  }

  def postChoiceRequest(body: JsValue): Request[AnyContentAsJson] =
    FakeRequest("POST", "")
      .withJsonBody(body)
      .withCSRFToken

  private def existingDeclaration(choice: DeclarationType = DeclarationType.SUPPLEMENTARY) =
    aDeclaration(withId("existingDeclarationId"), withType(choice))

  "ChoiceController displayPage" should {

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
        val form = Choice.form.fill(Choice(CancelDec))

        viewOf(result) must be(choicePage(form, allJourneys)(request, controller.messagesApi.preferred(request)))
      }

      "cache contains existing declaration" in {
        withNewCaching(existingDeclaration())

        val request = getRequest()
        val result = controller.displayPage(Some(Choice(Dashboard)))(request)
        val form = Choice.form.fill(Choice(Dashboard))

        viewOf(result) must be(choicePage(form, allJourneys)(request, controller.messagesApi.preferred(request)))
      }
    }

    "not select any choice " when {

      "choice parameter not given and cache empty" in {
        withNoDeclaration()

        val request = getRequest()
        val result = controller.displayPage(None)(request)
        val form = Choice.form

        viewOf(result) must be(choicePage(form, allJourneys)(request, controller.messagesApi.preferred(request)))
      }
    }
  }

  "ChoiceController submitChoice" should {

    "return 400 (BAD_REQUEST)" when {
      "form is incorrect" in {
        val result = controller.submitChoice()(postChoiceRequest(incorrectChoice))

        status(result) must be(BAD_REQUEST)
        verifyTheCacheIsUnchanged()
      }
    }

    "redirect to Declaration choice page" when {
      "user chooses Create Dec " in {
        val result = controller.submitChoice()(postChoiceRequest(createChoice))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(DeclarationChoiceController.displayPage.url))
        verifyTheCacheIsUnchanged()
      }
    }

    "redirect to movements service" when {
      "user selects Arrive, depart or consolidate a dec" in {
        val result = controller.submitChoice()(postChoiceRequest(movementsChoice))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(externalServicesConfig.customsMovementsFrontendUrl))
        verifyTheCacheIsUnchanged()
      }
    }

    "redirect to Cancel Declaration page" when {
      "user chose Cancel Dec" in {
        val result = controller.submitChoice()(postChoiceRequest(cancelChoice))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(routes.CancelDeclarationController.displayPage.url))
        verifyTheCacheIsUnchanged()
      }
    }

    "redirect to the Dashboard" when {
      "user chooses to view the list of submissions" in {
        val result = controller.submitChoice()(postChoiceRequest(dashboardChoice))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(toDashboard.url))
        verifyTheCacheIsUnchanged()
      }
    }

    "redirect to Saved Declarations page" when {
      "user chose continue a saved declaration" in {
        val result = controller.submitChoice()(postChoiceRequest(continueDeclarationChoice))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(routes.SavedDeclarationsController.displayDeclarations().url))
        verifyTheCacheIsUnchanged()
      }
    }

    "redirect to Exports secure messaging inbox page" when {
      "user chose view messages" in {
        val result = controller.submitChoice()(postChoiceRequest(inboxChoice))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(routes.SecureMessagingController.displayInbox.url))
        verifyTheCacheIsUnchanged()
      }
    }
  }

  "ChoiceController availableJourneys" should {
    "contain all journey types when isExportsSecureMessagingEnabled returns true" in {
      when(mockSecureMessagingInboxConfig.isExportsSecureMessagingEnabled).thenReturn(true)

      val choiceCtrl = new ChoiceController(
        mockAuthAction,
        mockVerifiedEmailAction,
        stubMessagesControllerComponents(),
        mockSecureMessagingInboxConfig,
        choicePage,
        appConfig,
        externalServicesConfig
      )
      allJourneys.diff(choiceCtrl.availableJourneys).size mustBe 0
    }

    "contain all journey types apart from 'Inbox' when isExportsSecureMessagingEnabled returns false" in {
      when(mockSecureMessagingInboxConfig.isExportsSecureMessagingEnabled).thenReturn(false)

      val choiceCtrl = new ChoiceController(
        mockAuthAction,
        mockVerifiedEmailAction,
        stubMessagesControllerComponents(),
        mockSecureMessagingInboxConfig,
        choicePage,
        appConfig,
        externalServicesConfig
      )
      val missingJourneyTypes = allJourneys.diff(choiceCtrl.availableJourneys)
      missingJourneyTypes.size mustBe 1
      missingJourneyTypes must contain(Inbox)
    }
  }
}

object ChoiceControllerSpec {
  val incorrectChoice: JsValue = Json.toJson(Choice("Incorrect Choice"))
  val createChoice: JsValue = Json.toJson(Choice(CreateDec))
  val movementsChoice: JsValue = Json.toJson(Choice(Movements))
  val cancelChoice: JsValue = Json.toJson(Choice(CancelDec))
  val dashboardChoice: JsValue = Json.toJson(Choice(Dashboard))
  val continueDeclarationChoice: JsValue = Json.toJson(Choice(ContinueDec))
  val inboxChoice: JsValue = Json.toJson(Choice(Inbox))
}
