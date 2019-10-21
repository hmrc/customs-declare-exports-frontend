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

import controllers.declaration.{routes, CommodityMeasureController}
import forms.Choice
import forms.declaration.{CommodityMeasure, PackageInformation}
import models.{DeclarationType, Mode}
import models.declaration.ExportItem
import play.api.libs.json.Json
import play.api.test.Helpers._
import unit.base.ControllerSpec
import views.html.declaration.goods_measure

class CommodityMeasureControllerSpec extends ControllerSpec {

  trait SetUp {
    val goodsMeasurePage = new goods_measure(mainTemplate)

    val controller = new CommodityMeasureController(
      mockAuthAction,
      mockJourneyAction,
      mockExportsCacheService,
      navigator,
      stubMessagesControllerComponents(),
      goodsMeasurePage
    )(ec)

    authorizedUser()

    val item =
      ExportItem("itemId", packageInformation = List(PackageInformation("123", 123, "123")))
    val cachedData = aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item))

    withNewCaching(cachedData)
  }

  "Commodity Measure controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and commodity measure cache is empty" in new SetUp {

        val result = controller.displayPage(Mode.Normal, "itemId")(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked and commodity measure cache is not empty" in new SetUp {

        val commodityItem = item.copy(commodityMeasure = Some(CommodityMeasure(None, "123", "123")))
        val commodityCachedData =
          aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item))
        withNewCaching(commodityCachedData)

        val result = controller.displayPage(Mode.Normal, "itemId")(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "display page method is invoked and commodity package information cache is empty" in new SetUp {
        val noPackageCache = aDeclaration(withType(DeclarationType.SUPPLEMENTARY))
        withNewCaching(noPackageCache)

        val result = controller.displayPage(Mode.Normal, "itemId")(getRequest())

        status(result) must be(BAD_REQUEST)
      }

      "form is incorrect" in new SetUp {

        val noPackageCache = aDeclaration(withType(DeclarationType.SUPPLEMENTARY))
        withNewCaching(noPackageCache)

        val incorrectForm = Json.toJson(CommodityMeasure(None, "", ""))

        val result = controller.submitForm(Mode.Normal, "itemId")(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "information provided by user are correct" in new SetUp {

        val noPackageCache = aDeclaration(withType(DeclarationType.SUPPLEMENTARY))
        withNewCaching(noPackageCache)

        val correctForm = Json.toJson(CommodityMeasure(None, "1234.12", "1234.12"))

        val result = controller.submitForm(Mode.Normal, "itemId")(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.AdditionalInformationController.displayPage(Mode.Normal, "itemId")
      }
    }
  }
}
