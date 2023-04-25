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

import base.{ControllerSpec, Injector}
import controllers.declaration.routes.{SealController, TransportContainerController}
import controllers.helpers.Remove
import forms.common.YesNoAnswer
import forms.declaration.{ContainerAdd, ContainerFirst, Seal}
import mock.ErrorHandlerMocks
import models.DeclarationType
import models.declaration.Container.maxNumberOfItems
import models.declaration.Container
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.Assertion
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration._

class TransportContainerControllerSpec extends ControllerSpec with ErrorHandlerMocks with Injector {

  val transportContainersAddFirstPage = instanceOf[transport_container_add_first]
  val transportContainersAddPage = instanceOf[transport_container_add]
  val transportContainersRemovePage = instanceOf[transport_container_remove]
  val transportContainersSummaryPage = mock[transport_container_summary]

  val controller = new TransportContainerController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    mockExportsCacheService,
    stubMessagesControllerComponents(),
    transportContainersAddFirstPage,
    transportContainersAddPage,
    transportContainersSummaryPage,
    transportContainersRemovePage
  )(ec)

  val containerId = "434335468"
  val sealId = "287345"

  val containerData = Container(1, containerId, Seq(Seal(1, sealId)))
  val maxContainerData = (1 to maxNumberOfItems).map(Container(_, "id", Seq.empty))

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    setupErrorHandler()
    withNewCaching(aDeclaration(withType(DeclarationType.STANDARD)))

    when(transportContainersSummaryPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(transportContainersSummaryPage)
    super.afterEach()
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(transportContainersSummaryPage).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withContainerData(containerData)))
    await(controller.displayContainerSummary()(request))
    theResponseForm
  }

  "Transport Container controller display add page" should {

    "return 200 (OK)" when {

      "cache is empty" in {
        val result = controller.displayAddContainer()(getRequest())
        status(result) must be(OK)
      }

      "cache contains some data" in {
        withNewCaching(aDeclaration(withContainerData(containerData)))
        val result = controller.displayAddContainer()(getRequest())
        status(result) must be(OK)
      }
    }
  }

  "Transport Container controller display container summary page" should {

    "return 200 (OK)" when {
      "cache contains some data" in {
        withNewCaching(aDeclaration(withContainerData(containerData)))

        val result = controller.displayContainerSummary()(getRequest())
        status(result) must be(OK)
      }
    }

    "redirect to add container page" when {
      "cache is empty" in {
        val result = controller.displayContainerSummary()(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe TransportContainerController.displayAddContainer
      }
    }
  }

  "Transport Container controller display container remove page" should {

    "return 200 (OK)" when {
      "cache contains some data" in {
        withNewCaching(aDeclaration(withContainerData(containerData)))

        val result = controller.displayContainerRemove(containerId)(getRequest())
        status(result) must be(OK)
      }
    }

    "redirect to container summary page" when {
      "cache is empty" in {
        val result = controller.displayContainerRemove(containerId)(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe TransportContainerController.displayContainerSummary
      }
    }
  }

  "Transport Container submit add page" should {

    "add first container and redirect to add seal page" when {
      val requestBody = Seq(ContainerFirst.hasContainerKey -> "Yes", ContainerFirst.containerIdKey -> "value")

      "working on standard declaration with cache empty" in {
        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD)))

        val result = controller.submitAddContainer()(postRequestAsFormUrlEncoded(requestBody: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe SealController.displaySealSummary("value")

        verifyCachedContainers(Seq(Container(1, "value", Seq.empty)), 1)
      }

      "working on supplementary declaration with cache empty" in {
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))

        val result = controller.submitAddContainer()(postRequestAsFormUrlEncoded(requestBody: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe SealController.displaySealSummary("value")

        verifyCachedContainers(Seq(Container(1, "value", Seq.empty)), 1)
      }

      "working on simplified declaration with cache empty" in {
        withNewCaching(aDeclaration(withType(DeclarationType.SIMPLIFIED)))

        val result = controller.submitAddContainer()(postRequestAsFormUrlEncoded(requestBody: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe SealController.displaySealSummary("value")

        verifyCachedContainers(Seq(Container(1, "value", Seq.empty)), 1)
      }
    }

    "add another container and redirect to add seal page" when {
      val requestBody = Seq(ContainerAdd.containerIdKey -> "C2")

      "working on standard declaration with existing container" in {
        withNewCaching(
          aDeclaration(
            withType(DeclarationType.STANDARD),
            withMaxSeqIds(Map(Container.seqIdKey -> 1)),
            withContainerData(Container(1, "C1", Seq.empty))
          )
        )

        val result = controller.submitAddContainer()(postRequestAsFormUrlEncoded(requestBody: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe SealController.displaySealSummary("C2")

        verifyCachedContainers(Seq(Container(1, "C1", Seq.empty), Container(2, "C2", Seq.empty)), 2)
      }

      "working on supplementary declaration with existing container" in {
        withNewCaching(
          aDeclaration(
            withType(DeclarationType.SUPPLEMENTARY),
            withMaxSeqIds(Map(Container.seqIdKey -> 1)),
            withContainerData(Container(1, "C1", Seq.empty))
          )
        )

        val result = controller.submitAddContainer()(postRequestAsFormUrlEncoded(requestBody: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe SealController.displaySealSummary("C2")

        verifyCachedContainers(Seq(Container(1, "C1", Seq.empty), Container(2, "C2", Seq.empty)), 2)
      }

      "working on simplified declaration with existing container" in {
        withNewCaching(
          aDeclaration(
            withType(DeclarationType.SIMPLIFIED),
            withMaxSeqIds(Map(Container.seqIdKey -> 1)),
            withContainerData(Container(1, "C1", Seq.empty))
          )
        )

        val result = controller.submitAddContainer()(postRequestAsFormUrlEncoded(requestBody: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe SealController.displaySealSummary("C2")

        verifyCachedContainers(Seq(Container(1, "C1", Seq.empty), Container(2, "C2", Seq.empty)), 2)
      }
    }
  }

  "Transport Container submit summary page" should {

    "redirect to confirmation page" when {
      "user clicks on remove container" in {
        val removeAction = (Remove.toString, "value")
        val result = controller.submitSummaryAction()(postRequestAsFormUrlEncoded(removeAction))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe TransportContainerController.displayContainerRemove("value")
      }
    }

    "redirect to add container page" when {
      "user indicates they want to add another container" in {
        val body = Json.obj("yesNo" -> "Yes")
        val result = controller.submitSummaryAction()(postRequest(body))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe TransportContainerController.displayAddContainer
      }

      "user indicates they want to add another container in error-fix mode" in {
        val body = Json.obj("yesNo" -> "Yes")
        val result = controller.submitSummaryAction()(postRequest(body))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe TransportContainerController.displayAddContainer
      }
    }

    "redirect to summary page" when {

      "user indicates they do not want to add another container" in {
        val body = Json.obj("yesNo" -> "No")
        val result = controller.submitSummaryAction()(postRequest(body))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.SummaryController.displayPage
      }

      "user indicates they do not want to add another container and they are in draft mode" in {
        val body = Json.obj("yesNo" -> "No")
        val result = controller.submitSummaryAction()(postRequest(body))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.SummaryController.displayPage
      }
    }
  }

  "Transport Container submit remove page" should {

    "remove container and redirect" when {
      "user confirms that they want to remove" in {
        withNewCaching(aDeclaration(withContainerData(containerData), withMaxSeqIds(Map(Container.seqIdKey -> 1))))

        val body = Json.obj("yesNo" -> "Yes")
        val result = controller.submitContainerRemove(containerId)(postRequest(body))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe TransportContainerController.displayContainerSummary

        verifyCachedContainers(Seq.empty, 1)
      }
    }

    "not remove container and redirect" when {
      "user confirms that they do not want to remove" in {
        withNewCaching(aDeclaration(withContainerData(containerData)))

        val body = Json.obj("yesNo" -> "No")
        val result = controller.submitContainerRemove(containerId)(postRequest(body))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe TransportContainerController.displayContainerSummary

        verifyTheCacheIsUnchanged()
      }
    }
  }

  "return 400 (BAD_REQUEST)" when {

    "user adds container with incorrect item" in {
      val body = Json.obj("id" -> "!@#$")
      val result = controller.submitAddContainer()(postRequest(body))

      status(result) must be(BAD_REQUEST)
    }

    "user adds seal and reached limit of items" in {
      withNewCaching(aDeclaration(withContainerData(maxContainerData: _*)))

      val body = Json.obj("id" -> "value")
      val result = controller.submitAddContainer()(postRequest(body))

      status(result) must be(BAD_REQUEST)
    }

    "user adds seal with duplicated value" in {
      withNewCaching(aDeclaration(withContainerData(containerData)))

      val body = Json.obj("id" -> containerId)
      val result = controller.submitAddContainer()(postRequest(body))

      status(result) must be(BAD_REQUEST)
    }
  }

  def verifyCachedContainers(expectedContainers: Seq[Container], expectedSequenceId: Int): Assertion = {
    val declaration = theCacheModelUpdated
    declaration.containers mustBe expectedContainers
    declaration.declarationMeta.maxSequenceIds.get(Container.seqIdKey) mustBe Some(expectedSequenceId)
  }
}
