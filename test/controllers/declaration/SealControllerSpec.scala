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

import base.ControllerSpec
import controllers.declaration.routes.{SealController, TransportContainerController}
import controllers.helpers.{Remove, SaveAndContinue, SequenceIdHelper}
import forms.common.YesNoAnswer
import forms.declaration.Seal
import mock.ErrorHandlerMocks
import models.declaration.{Container, ExportItem}
import models.{DeclarationMeta, DeclarationType}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.{Assertion, GivenWhenThen}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.{seal_add, seal_remove, seal_summary}

class SealControllerSpec extends ControllerSpec with ErrorHandlerMocks with GivenWhenThen {

  val sealAddPage = mock[seal_add]
  val sealRemovePage = mock[seal_remove]
  val sealSummaryPage = mock[seal_summary]

  val containerId = "3436532313"
  val sealId = "623847987324"

  private val mockSeqIdHandler = mock[SequenceIdHelper]

  val controller = new SealController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    mockErrorHandler,
    mockExportsCacheService,
    stubMessagesControllerComponents(),
    sealAddPage,
    sealRemovePage,
    sealSummaryPage,
    mockSeqIdHandler
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    setupErrorHandler()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))
    when(sealAddPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(sealRemovePage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(sealSummaryPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockSeqIdHandler.handleSequencing[ExportItem](any(), any())(any())).thenAnswer(new Answer[(Seq[ExportItem], DeclarationMeta)] {
      def answer(invocation: InvocationOnMock): (Seq[ExportItem], DeclarationMeta) = {
        val args = invocation.getArguments
        (args(0).asInstanceOf[Seq[ExportItem]], args(1).asInstanceOf[DeclarationMeta])
      }
    })
  }

  override protected def afterEach(): Unit = {
    reset(sealAddPage, sealRemovePage, sealSummaryPage, mockSeqIdHandler)

    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withRoutingCountries()))
    await(controller.displaySealSummary(containerId)(request))
    theResponseSummaryForm
  }

  def theResponseSummaryForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(sealSummaryPage).apply(captor.capture(), any(), any())(any(), any())
    captor.getValue
  }

  "Seal Controller" should {

    "return 200 (OK)" when {

      "display add seal page when cache empty" in {
        val result = controller.displayAddSeal(containerId)(getRequest())
        status(result) must be(OK)
      }

      "display add seal page when cache contain some data" in {
        withNewCaching(aDeclaration(withContainerData(Container(1, containerId, Seq.empty))))

        val result = controller.displayAddSeal(containerId)(getRequest())
        status(result) must be(OK)
      }

      "display seal summary page when cache empty" in {
        val result = controller.displaySealSummary(containerId)(getRequest())
        status(result) must be(OK)
      }

      "display seal summary page when cache contain some data" in {
        withNewCaching(aDeclaration(withContainerData(Container(1, containerId, Seq.empty))))

        val result = controller.displaySealSummary(containerId)(getRequest())
        status(result) must be(OK)
      }

      "display remove seal page when cache empty" in {
        val result = controller.displaySealRemove(containerId, sealId)(getRequest())
        status(result) must be(OK)
      }

      "display remove seal page when cache contain some data" in {
        withNewCaching(aDeclaration(withContainerData(Container(1, containerId, Seq(Seal(1, sealId))))))

        val result = controller.displaySealRemove(containerId, sealId)(getRequest())
        status(result) must be(OK)
      }

    }

    "return 400 (BAD_REQUEST)" when {

      "user adds seal when container not in cache" in {
        val body = Json.obj("id" -> "value")

        val result = controller.submitAddSeal(containerId)(postRequest(body))

        status(result) must be(BAD_REQUEST)
      }

      "user adds seal with incorrect item" in {
        withNewCaching(aDeclaration(withContainerData(Container(1, containerId, Seq.empty))))
        val body = Json.obj("id" -> "!@#$")

        val result = controller.submitAddSeal(containerId)(postRequest(body))

        status(result) must be(BAD_REQUEST)
      }

      "user adds seal and reached limit of items" in {
        val seals = (1 to 9999).map(Seal(_, "id"))
        withNewCaching(aDeclaration(withContainerData(Container(1, containerId, seals))))

        val body = Seq(("id", "value"))

        val result = controller.submitAddSeal(containerId)(postRequestAsFormUrlEncoded(body: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user adds seal with duplicated value" in {
        withNewCaching(aDeclaration(withContainerData(Container(1, containerId, Seq(Seal(1, "value"))))))

        val body = Json.obj("id" -> "value")

        val result = controller.submitAddSeal(containerId)(postRequest(body))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "add the first seal" in {
        withNewCaching(aDeclaration(withContainerData(Container(id = containerId, seals = Seq.empty))))
        val body = Seq("id" -> "value", (SaveAndContinue.toString, ""))

        val result = controller.submitAddSeal(containerId)(postRequestAsFormUrlEncoded(body: _*))

        await(result) mustBe aRedirectToTheNextPage

        verifyCachedSeals(Seq(Container(id = containerId, seals = Seq(Seal(id = "value")))))

        thePageNavigatedTo mustBe SealController.displaySealSummary(containerId)

        And("max seq Id is updated")
        verify(mockSeqIdHandler).handleSequencing[Container](any(), any())(any())
      }

      "add an additional seal" in {
        val seal1 = Seal(id = "seal1")
        withNewCaching(aDeclaration(withContainerData(Container(id = containerId, seals = Seq(seal1)))))

        val body = Seq("id" -> "seal2", (SaveAndContinue.toString, ""))
        val result = controller.submitAddSeal(containerId)(postRequestAsFormUrlEncoded(body: _*))

        await(result) mustBe aRedirectToTheNextPage

        verifyCachedSeals(Seq(Container(id = containerId, seals = Seq(seal1, Seal(id = "seal2")))))

        thePageNavigatedTo mustBe SealController.displaySealSummary(containerId)

        And("max seq Id is updated")
        verify(mockSeqIdHandler).handleSequencing[Seal](any(), any())(any())
      }

      "add the first seal to an additional container" in {
        withNewCaching(
          aDeclaration(
            withContainerData(Container(id = containerId, seals = Seq(Seal(id = "seal1"))), Container(id = "container2", seals = Seq.empty))
          )
        )

        val body = Seq("id" -> "seal2", (SaveAndContinue.toString, ""))
        val result = controller.submitAddSeal("container2")(postRequestAsFormUrlEncoded(body: _*))

        await(result) mustBe aRedirectToTheNextPage

        verifyCachedSeals(
          Seq(Container(id = containerId, seals = Seq(Seal(id = "seal1"))), Container(id = "container2", seals = Seq(Seal(id = "seal2"))))
        )

        thePageNavigatedTo mustBe SealController.displaySealSummary("container2")

        And("max seq Id is updated")
        verify(mockSeqIdHandler).handleSequencing[Seal](any(), any())(any())
      }

      "add a seal and" when {
        "user clicks 'save and return" in {
          withNewCaching(aDeclaration(withContainerData(Container(id = containerId, seals = Seq.empty))))
          val body = Seq("id" -> "value", ("SaveAndReturn", ""))

          val result = controller.submitAddSeal(containerId)(postRequestAsFormUrlEncoded(body: _*))

          await(result) mustBe aRedirectToTheNextPage

          verify(mockSeqIdHandler).handleSequencing[Seal](any(), any())(any())
          verifyCachedSeals(Seq(Container(id = containerId, seals = Seq(Seal(id = "value")))))

          thePageNavigatedTo mustBe SealController.displaySealSummary(containerId)
        }
      }

      "remove seal asks for confirmation" when {

        "user clicks 'remove' when container not in cache" in {
          val removeAction = (Remove.toString, "value")

          val result = controller.submitSummaryAction(containerId)(postRequestAsFormUrlEncoded(removeAction))

          await(result) mustBe aRedirectToTheNextPage

          thePageNavigatedTo mustBe SealController.displaySealRemove(containerId, "value")
        }

        "user clicks 'remove' when container in cache" in {
          withNewCaching(aDeclaration(withContainerData(Container(id = containerId, seals = Seq(Seal(id = "value"))))))
          val removeAction = (Remove.toString, "value")

          val result = controller.submitSummaryAction(containerId)(postRequestAsFormUrlEncoded(removeAction))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe SealController.displaySealRemove(containerId, "value")
        }
      }

      "add another seal question" when {

        "redirects when user answers 'Yes" in {
          val body = Json.obj("yesNo" -> "Yes")

          val result = controller.submitSummaryAction(containerId)(postRequest(body))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe SealController.displayAddSeal(containerId)
        }

        "redirects when user answers 'Yes' in error-fix mode" in {
          val body = Json.obj("yesNo" -> "Yes")

          val result = controller.submitSummaryAction(containerId)(postRequest(body))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe SealController.displayAddSeal(containerId)
        }

        "redirects when user answers 'No" in {
          val body = Json.obj("yesNo" -> "No")

          val result = controller.submitSummaryAction(containerId)(postRequest(body))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe TransportContainerController.displayContainerSummary
        }
      }

      "remove seal confirmation" when {

        "user confirms that they want to remove" in {
          val container1 = Container(id = containerId, seals = Seq(Seal(id = sealId)))
          val container2 = Container(id = "containerB", seals = Seq(Seal(id = "sealB")))
          withNewCaching(aDeclaration(withContainerData(container1, container2)))

          val body = Json.obj("yesNo" -> "Yes")
          val result = controller.submitSealRemove(containerId, sealId)(postRequest(body))

          await(result) mustBe aRedirectToTheNextPage

          verify(mockSeqIdHandler).handleSequencing[Seal](any(), any())(any())
          verifyCachedSeals(Seq(Container(id = containerId, seals = Seq.empty), container2))

          thePageNavigatedTo mustBe SealController.displaySealSummary(containerId)
        }

        "user confirms that they do not want to remove" in {
          withNewCaching(aDeclaration(withContainerData(Container(id = containerId, seals = Seq(Seal(id = sealId))))))
          val body = Json.obj("yesNo" -> "No")

          val result = controller.submitSealRemove(containerId, sealId)(postRequest(body))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe SealController.displaySealSummary(containerId)

          verifyTheCacheIsUnchanged()
        }
      }

      def verifyCachedSeals(expectedContainers: Seq[Container]): Assertion = {
        val declaration = theCacheModelUpdated
        declaration.containers.toList mustBe expectedContainers
      }
    }
  }
}
