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

import base.{AuditedControllerSpec, ControllerSpec, Injector}
import controllers.declaration.routes.{SealController, TransportContainerController}
import controllers.helpers.Remove
import controllers.helpers.SequenceIdHelper.valueOfEso
import forms.common.YesNoAnswer
import forms.declaration.{ContainerAdd, ContainerFirst, Seal}
import mock.ErrorHandlerMocks
import models.DeclarationType
import models.declaration.Container
import models.declaration.Container.maxNumberOfItems
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.{Assertion, OptionValues}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration._

class TransportContainerControllerSpec extends ControllerSpec with AuditedControllerSpec with ErrorHandlerMocks with Injector with OptionValues {

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
  )(ec, auditService)

  val containerId = "434335468"
  val sealId = "287345"

  val containerData = Container(1, containerId, List(Seal(1, sealId)))
  val maxContainerData = (1 to maxNumberOfItems).map(Container(_, "id", Seq.empty))

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    setupErrorHandler()
    withNewCaching(aDeclaration(withType(DeclarationType.STANDARD)))

    when(transportContainersSummaryPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(transportContainersSummaryPage, auditService)
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
      val requestBody = List(ContainerFirst.hasContainerKey -> "Yes", ContainerFirst.containerIdKey -> "value")

      "working on standard declaration with cache empty" in {
        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD)))

        val result = controller.submitAddContainer()(postRequestAsFormUrlEncoded(requestBody: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe SealController.displaySealSummary("value")

        verifyCachedContainers(1, List(Container(sequenceId = 1, id = "value", seals = Seq.empty)))
      }

      "working on supplementary declaration with cache empty" in {
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))

        val result = controller.submitAddContainer()(postRequestAsFormUrlEncoded(requestBody: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe SealController.displaySealSummary("value")

        verifyCachedContainers(1, List(Container(sequenceId = 1, id = "value", seals = Seq.empty)))
      }

      "working on simplified declaration with cache empty" in {
        withNewCaching(aDeclaration(withType(DeclarationType.SIMPLIFIED)))

        val result = controller.submitAddContainer()(postRequestAsFormUrlEncoded(requestBody: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe SealController.displaySealSummary("value")

        verifyCachedContainers(1, List(Container(sequenceId = 1, id = "value", seals = Seq.empty)))
      }
    }

    "add another container and redirect to add seal page" when {
      val requestBody = List(ContainerAdd.containerIdKey -> "C2")

      "working on standard declaration with existing container" in {
        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD), withContainerData(Container(id = "C1", seals = Seq.empty))))

        val result = controller.submitAddContainer()(postRequestAsFormUrlEncoded(requestBody: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe SealController.displaySealSummary("C2")

        verifyCachedContainers(
          0,
          List(Container(sequenceId = 0, id = "C1", seals = Seq.empty), Container(sequenceId = 0, id = "C2", seals = Seq.empty))
        )
      }

      "working on supplementary declaration with existing container" in {
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withContainerData(Container(id = "C1", seals = Seq.empty))))

        val result = controller.submitAddContainer()(postRequestAsFormUrlEncoded(requestBody: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe SealController.displaySealSummary("C2")

        verifyCachedContainers(
          0,
          List(Container(sequenceId = 0, id = "C1", seals = Seq.empty), Container(sequenceId = 0, id = "C2", seals = Seq.empty))
        )
      }

      "working on simplified declaration with existing container" in {
        withNewCaching(aDeclaration(withType(DeclarationType.SIMPLIFIED), withContainerData(Container(id = "C1", seals = Seq.empty))))

        val result = controller.submitAddContainer()(postRequestAsFormUrlEncoded(requestBody: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe SealController.displaySealSummary("C2")

        verifyCachedContainers(
          0,
          List(Container(sequenceId = 0, id = "C1", seals = Seq.empty), Container(sequenceId = 0, id = "C2", seals = Seq.empty))
        )
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

    "redirect to Section Summary page" when {

      "user indicates they do not want to add the first container" in {
        val requestBody = List(ContainerFirst.hasContainerKey -> "No", ContainerFirst.containerIdKey -> "")
        val result = controller.submitAddContainer()(postRequestAsFormUrlEncoded(requestBody: _*))

        await(result) mustBe aRedirectToTheNextPage
        theCacheModelUpdated(2).last.transport.containers mustBe Some(Nil)
        thePageNavigatedTo mustBe routes.SectionSummaryController.displayPage(6)
      }

      "user indicates they do not want to add another container" in {
        val body = Json.obj("yesNo" -> "No")
        val result = controller.submitSummaryAction()(postRequest(body))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.SectionSummaryController.displayPage(6)
      }

      "user indicates they do not want to add another container and they are in draft mode" in {
        val body = Json.obj("yesNo" -> "No")
        val result = controller.submitSummaryAction()(postRequest(body))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.SectionSummaryController.displayPage(6)
      }
    }
  }

  "Transport Container submit remove page" should {

    "remove container and redirect" when {
      "user confirms that they want to remove" in {
        withNewCaching(aDeclaration(withContainerData(containerData)))

        val body = Json.obj("yesNo" -> "Yes")
        val result = controller.submitContainerRemove(containerId)(postRequest(body))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe TransportContainerController.displayContainerSummary

        verifyCachedContainers(1, Seq.empty)
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
        verifyNoAudit()
      }
    }
  }

  "return 400 (BAD_REQUEST)" when {

    "user adds container with incorrect item" in {
      val body = Json.obj("id" -> "!@#$")
      val result = controller.submitAddContainer()(postRequest(body))

      status(result) must be(BAD_REQUEST)
      verifyNoAudit()
    }

    "user adds seal and reached limit of items" in {
      withNewCaching(aDeclaration(withContainerData(maxContainerData: _*)))

      val body = Json.obj("id" -> "value")
      val result = controller.submitAddContainer()(postRequest(body))

      status(result) must be(BAD_REQUEST)
      verifyNoAudit()
    }

    "user adds seal with duplicated value" in {
      withNewCaching(aDeclaration(withContainerData(containerData)))

      val body = Json.obj("id" -> containerId)
      val result = controller.submitAddContainer()(postRequest(body))

      status(result) must be(BAD_REQUEST)
      verifyNoAudit()
    }
  }

  def verifyCachedContainers(expectedSequenceId: Int, expectedContainers: Seq[Container]): Assertion = {
    val declaration = theCacheModelUpdated
    verifyAudit()
    declaration.containers mustBe expectedContainers
    valueOfEso[Container](declaration).value mustBe expectedSequenceId
  }
}
