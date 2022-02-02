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

import base.ControllerSpec
import controllers.helpers.{Remove, SaveAndContinue, SaveAndReturn}
import forms.common.YesNoAnswer
import forms.declaration.Seal
import mock.ErrorHandlerMocks
import models.declaration.Container
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.{seal_add, seal_remove, seal_summary}

class SealControllerSpec extends ControllerSpec with ErrorHandlerMocks {

  val sealAddPage = mock[seal_add]
  val sealRemovePage = mock[seal_remove]
  val sealSummaryPage = mock[seal_summary]

  val containerId = "3436532313"
  val sealId = "623847987324"

  val controller = new SealController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    mockErrorHandler,
    mockExportsCacheService,
    stubMessagesControllerComponents(),
    sealAddPage,
    sealRemovePage,
    sealSummaryPage
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    setupErrorHandler()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))
    when(sealAddPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(sealRemovePage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(sealSummaryPage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(sealAddPage, sealRemovePage, sealSummaryPage)

    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withRoutingCountries()))
    await(controller.displaySealSummary(Mode.Normal, containerId)(request))
    theResponseSummaryForm
  }

  def theResponseSummaryForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(sealSummaryPage).apply(any(), captor.capture(), any(), any())(any(), any())
    captor.getValue
  }

  "Seal Controller" should {

    "return 200 (OK)" when {

      "display add seal page when cache empty" in {

        val result = controller.displayAddSeal(Mode.Normal, containerId)(getRequest())
        status(result) must be(OK)
      }

      "display add seal page when cache contain some data" in {

        withNewCaching(aDeclaration(withContainerData(Container(containerId, Seq.empty))))

        val result = controller.displayAddSeal(Mode.Normal, containerId)(getRequest())
        status(result) must be(OK)
      }

      "display seal summary page when cache empty" in {

        val result = controller.displaySealSummary(Mode.Normal, containerId)(getRequest())
        status(result) must be(OK)
      }

      "display seal summary page when cache contain some data" in {

        withNewCaching(aDeclaration(withContainerData(Container(containerId, Seq.empty))))

        val result = controller.displaySealSummary(Mode.Normal, containerId)(getRequest())
        status(result) must be(OK)
      }

      "display remove seal page when cache empty" in {

        val result = controller.displaySealRemove(Mode.Normal, containerId, sealId)(getRequest())
        status(result) must be(OK)
      }

      "display remove seal page when cache contain some data" in {

        withNewCaching(aDeclaration(withContainerData(Container(containerId, Seq(Seal(sealId))))))

        val result = controller.displaySealRemove(Mode.Normal, containerId, sealId)(getRequest())
        status(result) must be(OK)
      }

    }

    "return 400 (BAD_REQUEST)" when {

      "user adds seal when container not in cache" in {

        val body = Json.obj("id" -> "value")

        val result = controller.submitAddSeal(Mode.Normal, containerId)(postRequest(body))

        status(result) must be(BAD_REQUEST)
      }

      "user adds seal with incorrect item" in {

        withNewCaching(aDeclaration(withContainerData(Container(containerId, Seq.empty))))
        val body = Json.obj("id" -> "!@#$")

        val result = controller.submitAddSeal(Mode.Normal, containerId)(postRequest(body))

        status(result) must be(BAD_REQUEST)
      }

      "user adds seal and reached limit of items" in {

        val seals = Seq.fill(9999)(Seal("id"))
        withNewCaching(aDeclaration(withContainerData(Container(containerId, seals))))

        val body = Seq(("id", "value"))

        val result = controller.submitAddSeal(Mode.Normal, containerId)(postRequestAsFormUrlEncoded(body: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user adds seal with duplicated value" in {

        withNewCaching(aDeclaration(withContainerData(Container(containerId, Seq(Seal("value"))))))

        val body = Json.obj("id" -> "value")

        val result = controller.submitAddSeal(Mode.Normal, containerId)(postRequest(body))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "add seal with data in the cache" when {

        "user clicks 'save and continue'" in {
          withNewCaching(aDeclaration(withContainerData(Container(containerId, Seq.empty))))
          val body = Seq("id" -> "value", (SaveAndContinue.toString, ""))

          val result = controller.submitAddSeal(Mode.Normal, containerId)(postRequestAsFormUrlEncoded(body: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.SealController
            .displaySealSummary(Mode.Normal, containerId)

          theCacheModelUpdated.containers mustBe Seq(Container(containerId, Seq(Seal("value"))))
        }

        "user clicks 'save and return" in {
          withNewCaching(aDeclaration(withContainerData(Container(containerId, Seq.empty))))
          val body = Seq("id" -> "value", (SaveAndReturn.toString, ""))

          val result = controller.submitAddSeal(Mode.Normal, containerId)(postRequestAsFormUrlEncoded(body: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.SealController
            .displaySealSummary(Mode.Normal, containerId)

          theCacheModelUpdated.containers mustBe Seq(Container(containerId, Seq(Seal("value"))))
        }
      }

      "remove seal asks for confirmation" when {

        "user clicks 'remove' when container not in cache" in {

          val removeAction = (Remove.toString, "value")

          val result =
            controller.submitSummaryAction(Mode.Normal, containerId)(postRequestAsFormUrlEncoded(removeAction))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.SealController
            .displaySealRemove(Mode.Normal, containerId, "value")
        }

        "user clicks 'remove' when container in cache" in {

          withNewCaching(aDeclaration(withContainerData(Container(containerId, Seq(Seal("value"))))))
          val removeAction = (Remove.toString, "value")

          val result =
            controller.submitSummaryAction(Mode.Normal, containerId)(postRequestAsFormUrlEncoded(removeAction))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.SealController
            .displaySealRemove(Mode.Normal, containerId, "value")
        }

      }

      "add another seal question" when {
        "redirects when user answers 'Yes" in {

          val body = Json.obj("yesNo" -> "Yes")

          val result = controller.submitSummaryAction(Mode.Normal, containerId)(postRequest(body))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.SealController
            .displayAddSeal(Mode.Normal, containerId)
        }

        "redirects when user answers 'Yes' in error-fix mode" in {

          val body = Json.obj("yesNo" -> "Yes")

          val result = controller.submitSummaryAction(Mode.ErrorFix, containerId)(postRequest(body))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.SealController
            .displayAddSeal(Mode.ErrorFix, containerId)
        }

        "redirects when user answers 'No" in {

          val body = Json.obj("yesNo" -> "No")

          val result = controller.submitSummaryAction(Mode.Normal, containerId)(postRequest(body))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.TransportContainerController
            .displayContainerSummary(Mode.Normal)
        }
      }

      "remove seal confirmation" when {
        "user confirms that they want to remove" in {

          withNewCaching(aDeclaration(withContainerData(Seq(Container(containerId, Seq(Seal(sealId))), Container("containerB", Seq(Seal("sealB")))))))
          val body = Json.obj("yesNo" -> "Yes")

          val result =
            controller.submitSealRemove(Mode.Normal, containerId, sealId)(postRequest(body))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.SealController
            .displaySealSummary(Mode.Normal, containerId)

          theCacheModelUpdated.containers mustBe Seq(Container(containerId, Seq.empty), Container("containerB", Seq(Seal("sealB"))))
        }

        "user confirms that they do not want to remove" in {

          withNewCaching(aDeclaration(withContainerData(Container(containerId, Seq(Seal(sealId))))))
          val body = Json.obj("yesNo" -> "No")

          val result =
            controller.submitSealRemove(Mode.Normal, containerId, sealId)(postRequest(body))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.SealController
            .displaySealSummary(Mode.Normal, containerId)

          verifyTheCacheIsUnchanged
        }
      }

    }
  }
}
