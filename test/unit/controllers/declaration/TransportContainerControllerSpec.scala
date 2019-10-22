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

import controllers.declaration.TransportContainerController
import controllers.util.Remove
import forms.Choice.AllowedChoiceValues.{StandardDec, SupplementaryDec}
import forms.declaration.Seal
import models.{DeclarationType, Mode}
import models.declaration.{Container, TransportInformationContainerData}
import play.api.test.Helpers._
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.html.declaration.{transport_container_add, transport_container_remove, transport_container_summary}

class TransportContainerControllerSpec extends ControllerSpec with ErrorHandlerMocks {

  trait SetUp {
    val transportContainersAddPage = new transport_container_add(mainTemplate)
    val transportContainersRemovePage = new transport_container_remove(mainTemplate)
    val transportContainersSummaryPage = new transport_container_summary(mainTemplate)

    val controller = new TransportContainerController(
      mockAuthAction,
      mockJourneyAction,
      navigator,
      mockErrorHandler,
      mockExportsCacheService,
      stubMessagesControllerComponents(),
      transportContainersAddPage,
      transportContainersSummaryPage,
      transportContainersRemovePage
    )(ec)

    authorizedUser()
    setupErrorHandler()
    withNewCaching(aDeclaration(withType(DeclarationType.STANDARD)))
  }

  val containerId = "434335468"
  val sealId = "287345"

  val containerData = TransportInformationContainerData(Seq(Container(containerId, Seq(Seal(sealId)))))
  val maxContainerData = TransportInformationContainerData(Seq.fill(TransportInformationContainerData.maxNumberOfItems)(Container("id", Seq.empty)))

  "Transport Container controller display add page" should {

    "return 200 (OK)" when {
      "cache is empty" in new SetUp {
        val result = controller.displayAddContainer(Mode.Normal)(getRequest())

        status(result) must be(OK)
      }

      "cache contains some data" in new SetUp {
        withNewCaching(aDeclaration(withContainerData(containerData)))

        val result = controller.displayAddContainer(Mode.Normal)(getRequest())
        status(result) must be(OK)
      }
    }
  }

  "Transport Container controller display container summary page" should {

    "return 200 (OK)" when {
      "cache contains some data" in new SetUp {
        withNewCaching(aDeclaration(withContainerData(containerData)))

        val result = controller.displayContainerSummary(Mode.Normal)(getRequest())
        status(result) must be(OK)
      }
    }

    "redirect to add container page" when {
      "cache is empty" in new SetUp {
        val result = controller.displayContainerSummary(Mode.Normal)(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.TransportContainerController
          .displayAddContainer(Mode.Normal)
      }
    }
  }

  "Transport Container controller display container remove page" should {

    "return 200 (OK)" when {
      "cache contains some data" in new SetUp {
        withNewCaching(aDeclaration(withContainerData(containerData)))

        val result = controller.displayContainerRemove(Mode.Normal, containerId)(getRequest())
        status(result) must be(OK)
      }
    }

    "redirect to container summary page" when {
      "cache is empty" in new SetUp {
        val result = controller.displayContainerRemove(Mode.Normal, containerId)(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.TransportContainerController
          .displayContainerSummary(Mode.Normal)
      }
    }
  }

  "Transport Container submit add page" should {

    "add new container and redirect to add seal page" when {
      "working on standard declaration with cache empty" in new SetUp {
        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD)))
        val body = Seq("id" -> "value")

        val result = controller.submitAddContainer(Mode.Normal)(postRequestAsFormUrlEncoded(body: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.SealController
          .displaySealSummary(Mode.Normal, "value")

        theCacheModelUpdated.containers mustBe Seq(Container("value", Seq.empty))
      }

      "working on simplified declaration with cache empty" in new SetUp {
        withNewCaching(aDeclaration(withType(DeclarationType.SIMPLIFIED)))
        val body = Seq("id" -> "value")

        val result = controller.submitAddContainer(Mode.Normal)(postRequestAsFormUrlEncoded(body: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.SealController
          .displaySealSummary(Mode.Normal, "value")

        theCacheModelUpdated.containers mustBe Seq(Container("value", Seq.empty))
      }
    }

    "add new container and redirect to container summary page" when {
      "working on supplementary declaration with cache empty" in new SetUp {
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))
        val body = Seq("id" -> "value")

        val result = controller.submitAddContainer(Mode.Normal)(postRequestAsFormUrlEncoded(body: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.TransportContainerController
          .displayContainerSummary(Mode.Normal)

        theCacheModelUpdated.containers mustBe Seq(Container("value", Seq.empty))
      }
    }
  }

  "Transport Container submit summary page" should {

    "redirect to confirmation page" when {
      "user clicks on remove container" in new SetUp {
        val removeAction = (Remove.toString, "value")

        val result = controller.submitSummaryAction(Mode.Normal)(postRequestAsFormUrlEncoded(removeAction))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.TransportContainerController
          .displayContainerRemove(Mode.Normal, "value")
      }
    }

    "redirect to add container page" when {
      "user indicates they want to add another container" in new SetUp {
        val body = Seq(("yesNo", "Yes"))

        val result = controller.submitSummaryAction(Mode.Normal)(postRequestAsFormUrlEncoded(body: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.TransportContainerController
          .displayAddContainer(Mode.Normal)
      }
    }

    "redirect to summary page" when {
      "user indicates they do not want to add another container" in new SetUp {
        val body = Seq(("yesNo", "No"))

        val result = controller.submitSummaryAction(Mode.Normal)(postRequestAsFormUrlEncoded(body: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.SummaryController.displayPage(Mode.Normal)
      }

      "user indicates they do not want to add another container and they are in draft mode" in new SetUp {
        val body = Seq(("yesNo", "No"))

        val result = controller.submitSummaryAction(Mode.Draft)(postRequestAsFormUrlEncoded(body: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.SummaryController.displayPage(Mode.Normal)
      }
    }

  }

  "Transport Container submit remove page" should {

    "remove container and redirect" when {
      "user confirms that they want to remove" in new SetUp {
        withNewCaching(aDeclaration(withContainerData(containerData)))
        val body = Seq(("yesNo", "Yes"))

        val result = controller.submitContainerRemove(Mode.Normal, containerId)(postRequestAsFormUrlEncoded(body: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.TransportContainerController
          .displayContainerSummary(Mode.Normal)

        theCacheModelUpdated.containers mustBe Seq.empty
      }
    }

    "not remove container and redirect" when {
      "user confirms that they do not want to remove" in new SetUp {
        withNewCaching(aDeclaration(withContainerData(containerData)))
        val body = Seq(("yesNo", "No"))

        val result = controller.submitContainerRemove(Mode.Normal, containerId)(postRequestAsFormUrlEncoded(body: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.TransportContainerController
          .displayContainerSummary(Mode.Normal)

        verifyTheCacheIsUnchanged
      }
    }
  }

  "return 400 (BAD_REQUEST)" when {
    "user adds container with incorrect item" in new SetUp {

      val body = Seq(("id", "!@#$"))

      val result = controller.submitAddContainer(Mode.Normal)(postRequestAsFormUrlEncoded(body: _*))

      status(result) must be(BAD_REQUEST)
    }

    "user adds seal and reached limit of items" in new SetUp {
      withNewCaching(aDeclaration(withContainerData(maxContainerData)))

      val body = Seq(("id", "value"))

      val result = controller.submitAddContainer(Mode.Normal)(postRequestAsFormUrlEncoded(body: _*))

      status(result) must be(BAD_REQUEST)
    }

    "user adds seal with duplicated value" in new SetUp {
      withNewCaching(aDeclaration(withContainerData(containerData)))

      val body = Seq(("id", containerId))

      val result = controller.submitAddContainer(Mode.Normal)(postRequestAsFormUrlEncoded(body: _*))

      status(result) must be(BAD_REQUEST)
    }
  }

}
