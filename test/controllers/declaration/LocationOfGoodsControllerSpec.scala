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

import base.{AuditedControllerSpec, ControllerSpec, MockTaggedCodes}
import connectors.CodeListConnector
import controllers.declaration.routes.OfficeOfExitController
import controllers.routes.RootController
import forms.declaration.LocationOfGoods
import forms.declaration.LocationOfGoods.locationId
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.SUPPLEMENTARY_EIDR
import models.DeclarationType
import models.codes.{Country, GoodsLocationCode}
import models.declaration.DeclarationStatus.AMENDMENT_DRAFT
import models.declaration.GoodsLocation
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.location_of_goods

import scala.collection.immutable.ListMap

class LocationOfGoodsControllerSpec extends ControllerSpec with AuditedControllerSpec with MockTaggedCodes with OptionValues {

  val mockLocationOfGoods = mock[location_of_goods]
  val mockCodeListConnector = mock[CodeListConnector]

  val controller = new LocationOfGoodsController(
    mockAuthAction,
    mockJourneyAction,
    stubMessagesControllerComponents(),
    mockLocationOfGoods,
    mockExportsCacheService,
    navigator,
    taggedAuthCodes
  )(ec, mockCodeListConnector, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))
    when(mockLocationOfGoods.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("PL" -> Country("Poland", "PL")))
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    reset(mockLocationOfGoods, mockCodeListConnector)
  }

  def theResponseForm: Form[LocationOfGoods] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[LocationOfGoods]])
    verify(mockLocationOfGoods).apply(captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    await(controller.displayPage(request))
    theResponseForm
  }

  "LocationOfGoodsController.displayPage" should {

    "return 200 (OK)" when {

      "the cache is empty" in {
        val result = controller.displayPage(getRequest())

        status(result) mustBe OK
        verify(mockLocationOfGoods).apply(any())(any(), any())

        theResponseForm.value mustBe empty
      }

      "the cache contains data and" when {

        "code is found in list" in {
          when {
            mockCodeListConnector.allGoodsLocationCodes(any())
          } thenReturn ListMap[String, GoodsLocationCode]("GBAUEMAEMAEMA" -> GoodsLocationCode("GBAUEMAEMAEMA", "Somwhere"))

          val locationOfGoods = LocationOfGoods("GBAUEMAEMAEMA")
          withNewCaching(aDeclaration(withGoodsLocation(locationOfGoods)))

          val result = controller.displayPage(getRequest())

          status(result) mustBe OK
          verify(mockLocationOfGoods).apply(any())(any(), any())

          theResponseForm.value mustNot be(empty)
          theResponseForm.value.value.code mustBe "GBAUEMAEMAEMA"
        }

        "code does not exist in list" in {
          when {
            mockCodeListConnector.allGoodsLocationCodes(any())
          } thenReturn ListMap[String, GoodsLocationCode]()

          val locationOfGoods = LocationOfGoods("GBAUEMAEMAEMA")
          withNewCaching(aDeclaration(withGoodsLocation(locationOfGoods)))

          val result = controller.displayPage()(getRequest())

          status(result) mustBe OK
          verify(mockLocationOfGoods).apply(any())(any(), any())

          theResponseForm.value mustNot be(empty)
          theResponseForm.value.value.code mustBe "GBAUEMAEMAEMA"
        }
      }
    }

    "return 303 (SEE_OTHER)" when {

      "the declaration is under amendment" in {
        withNewCaching(aDeclaration(withStatus(AMENDMENT_DRAFT)))
        val result = controller.displayPage(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe OfficeOfExitController.displayPage
      }

      "Additional dec type is Supplementary_EIDR with MOU" in {
        val holders = withAuthorisationHolders(Some(taggedAuthCodes.codesSkippingLocationOfGoods.head))
        withNewCaching(aDeclaration(withAdditionalDeclarationType(SUPPLEMENTARY_EIDR), holders))
        val result = controller.displayPage(getRequest())

        status(result) mustBe 303
        redirectLocation(result) mustBe Some(RootController.displayPage.url)
      }
    }
  }

  "LocationOfGoodsController.saveLocation" should {

    "update the declaration" when {
      "information provided by the user are correct" in {
        val correctForm = Json.obj("yesNo" -> "Yes", locationId -> "PLAUEMAEMAEMA", "code" -> "")
        val result = controller.saveLocation(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe OfficeOfExitController.displayPage
        verify(mockLocationOfGoods, times(0)).apply(any())(any(), any())

        theCacheModelUpdated.locations.goodsLocation.value mustBe GoodsLocation("PL", "A", "U", "EMAEMAEMA")
        verifyAudit()
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "no value is entered" in {
        val incorrectForm = Json.obj("yesNo" -> "", fieldIdOnError(locationId) -> "")
        val result = controller.saveLocation(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verifyNoAudit()

        theResponseForm.errors.head.messages.head mustBe "error.yesNo.required"
      }

      "no location code is entered" in {
        val incorrectForm = Json.obj("yesNo" -> "Yes", fieldIdOnError(locationId) -> "")
        val result = controller.saveLocation(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verifyNoAudit()

        theResponseForm.errors.head.messages.head mustBe "declaration.locationOfGoods.code.empty"
      }

      "the entered value is incorrect or not a list's option" in {
        val incorrectForm = Json.obj("yesNo" -> "Yes", fieldIdOnError(locationId) -> "!@#$")
        val result = controller.saveLocation(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verifyNoAudit()

        val errors = theResponseForm.errors
        errors(0).messages.head mustBe "declaration.locationOfGoods.code.error"
        errors(1).messages.head mustBe "declaration.locationOfGoods.code.error.length"
      }

      "form is incorrect" in {
        val incorrectForm = Json.toJson(LocationOfGoods("incorrect"))

        val result = controller.saveLocation(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verify(mockLocationOfGoods).apply(any())(any(), any())
        verifyNoAudit()
      }
    }

    "return 303 (SEE_OTHER)" when {
      "the declaration is under amendment" in {
        val declaration = aDeclaration(withStatus(AMENDMENT_DRAFT))
        withNewCaching(declaration)
        val result = controller.saveLocation(postRequest(JsString("")))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe OfficeOfExitController.displayPage

        verifyTheCacheIsUnchanged()
        verifyNoAudit()
      }
    }
  }
}
