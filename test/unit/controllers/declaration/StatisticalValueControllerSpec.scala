/*
 * Copyright 2020 HM Revenue & Customs
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

import controllers.declaration.StatisticalValueController
import forms.declaration.StatisticalValue
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.cache.ExportItemIdGeneratorService
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.html.declaration.statistical_value

class StatisticalValueControllerSpec extends ControllerSpec with ErrorHandlerMocks with OptionValues {

  val mockItemTypePage = mock[statistical_value]

  val controller = new StatisticalValueController(
    mockAuthAction,
    mockJourneyAction,
    mockErrorHandler,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockItemTypePage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(mockItemTypePage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  val itemId = new ExportItemIdGeneratorService().generateItemId()

  override protected def afterEach(): Unit = {
    reset(mockItemTypePage)
    super.afterEach()
  }

  def theResponseForm: Form[StatisticalValue] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[StatisticalValue]])
    verify(mockItemTypePage).apply(any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  def validateCache(itemType: StatisticalValue) = {
    val cacheItemType = theCacheModelUpdated.items.head.statisticalValue.getOrElse(StatisticalValue(""))
    cacheItemType mustBe itemType
  }

  "Item Type Controller" should {

    "return 200 (OK)" when {

      onJourney(DeclarationType.SUPPLEMENTARY, DeclarationType.STANDARD)() { declaration =>
        "item type exists in the cache and item type is defined" in {

          withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId), withStatisticalValue()))))

          val result = controller.displayPage(Mode.Normal, itemId)(getRequest(declaration))

          status(result) mustBe OK
          theResponseForm.value mustNot be(empty)
        }

        "item type exists in the cache and item type is not defined" in {

          withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId)))))

          val result = controller.displayPage(Mode.Normal, itemId)(getRequest(declaration))

          status(result) mustBe OK
          theResponseForm.value mustBe empty
        }

      }

    }

    "return 400 (BAD_REQUEST)" when {

      onJourney(DeclarationType.SUPPLEMENTARY, DeclarationType.STANDARD)() { declaration =>
        "invalid data is posted" in {

          withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId)))))

          val badData = Json.toJson(StatisticalValue("Seven"))

          val result = controller.submitItemType(Mode.Normal, itemId)(postRequest(badData))

          status(result) mustBe BAD_REQUEST
          verify(mockItemTypePage, times(1)).apply(any(), any(), any())(any(), any())
        }

      }
    }

    "return 303 (SEE_OTHER)" when {

      onJourney(DeclarationType.SIMPLIFIED, DeclarationType.OCCASIONAL, DeclarationType.CLEARANCE)() { declaration =>
        "invalid data is posted" in {

          withNewCaching(declaration)

          val result = controller.displayPage(Mode.Normal, itemId).apply(getRequest(declaration))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.StartController.displayStartPage.url)
        }

      }

      onJourney(DeclarationType.SUPPLEMENTARY, DeclarationType.STANDARD)() { declaration =>
        "valid data is posted" in {

          withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId)))))

          val badData = Json.toJson(StatisticalValue("7"))

          val result = controller.submitItemType(Mode.Normal, itemId)(postRequest(badData))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.PackageInformationController
            .displayPage(Mode.Normal, itemId)
          verify(mockItemTypePage, times(0)).apply(any(), any(), any())(any(), any())

          validateCache(StatisticalValue("7"))
        }

      }

    }
  }
}
