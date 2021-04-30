/*
 * Copyright 2021 HM Revenue & Customs
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
import controllers.declaration._
import forms.declaration.{CommodityMeasure, PackageInformation}
import models.declaration.ExportItem
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.commodityMeasure.commodity_measure

class CommodityMeasureControllerSpec extends ControllerSpec {

  val goodsMeasurePage = mock[commodity_measure]

  val controller = new CommodityMeasureController(
    mockAuthAction,
    mockVerifiedEmailAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    goodsMeasurePage
  )(ec)

  val item =
    ExportItem("itemId", packageInformation = Some(List(PackageInformation("id", Some("123"), Some(123), Some("123")))))

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(goodsMeasurePage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    reset(goodsMeasurePage)
  }

  def theResponseForm: Form[CommodityMeasure] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[CommodityMeasure]])
    verify(goodsMeasurePage).apply(any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal, "itemId")(request))
    theResponseForm
  }

  "Commodity Measure controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and commodity measure cache is empty" in {

        withNewCaching(aDeclaration())
        val result = controller.displayPage(Mode.Normal, "itemId")(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked and commodity measure cache is not empty" in {

        val commodityCachedData =
          aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item))
        withNewCaching(commodityCachedData)

        val result = controller.displayPage(Mode.Normal, "itemId")(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in {

        val noPackageCache = aDeclaration(withType(DeclarationType.SUPPLEMENTARY))
        withNewCaching(noPackageCache)

        val incorrectForm = Json.toJson(CommodityMeasure(None, None, None))

        val result = controller.submitForm(Mode.Normal, "itemId")(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "information provided by user are correct" in {

        val noPackageCache = aDeclaration(withType(DeclarationType.SUPPLEMENTARY))
        withNewCaching(noPackageCache)

        val correctForm = Json.toJson(CommodityMeasure(None, Some("1234.12"), Some("1234.12")))

        val result = controller.submitForm(Mode.Normal, "itemId")(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.AdditionalInformationRequiredController.displayPage(Mode.Normal, "itemId")
      }
    }
  }
}
