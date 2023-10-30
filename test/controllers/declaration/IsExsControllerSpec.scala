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
import controllers.declaration.routes.{ConsigneeDetailsController, ConsignorEoriNumberController, RepresentativeAgentController}
import forms.common.YesNoAnswer.YesNoAnswers
import forms.common.{Address, Eori}
import forms.declaration.consignor.ConsignorDetails
import forms.declaration.{EntityDetails, IsExs, UNDangerousGoodsCode}
import models.DeclarationType
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.is_exs

class IsExsControllerSpec extends ControllerSpec with ScalaFutures {

  private val isExsPage = mock[is_exs]

  private val controller =
    new IsExsController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, stubMessagesControllerComponents(), isExsPage)(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(isExsPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(isExsPage)

    super.afterEach()
  }

  private def theResponseForm: Form[IsExs] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[IsExs]])
    verify(isExsPage).apply(formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withType(DeclarationType.CLEARANCE)))
    await(controller.displayPage(request))
    theResponseForm
  }

  "IsExsController on displayOutcomePage" should {

    "return 200 (OK)" when {

      "display page method is invoked without data in cache" in {
        withNewCaching(aDeclaration(withType(DeclarationType.CLEARANCE), withoutIsExs))

        val result = controller.displayPage(getRequest())

        status(result) mustBe OK
      }

      "display page method is invoked with data in cache" in {
        withNewCaching(aDeclaration(withType(DeclarationType.CLEARANCE), withIsExs()))

        val result = controller.displayPage(getRequest())

        status(result) mustBe OK
      }
    }
  }

  "IsExsController on submit" when {

    "answer is missing" should {
      "return 400 (BAD_REQUEST)" in {
        withNewCaching(aDeclaration(withType(DeclarationType.CLEARANCE), withDeclarantIsExporter()))

        val emptyForm = Json.toJson(IsExs(""))

        val result = controller.submit()(postRequest(emptyForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "answer is Yes" should {
      val correctForm = Json.toJson(IsExs(YesNoAnswers.yes))

      "return 303 (SEE_OTHER) and redirect to Consignor Eori page" in {

        withNewCaching(aDeclaration(withType(DeclarationType.CLEARANCE)))

        val result = controller.submit()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe ConsignorEoriNumberController.displayPage
      }

      "correctly update UNDangerous goods codes for items" in {
        withNewCaching(
          aDeclaration(
            withType(DeclarationType.CLEARANCE),
            withItem(anItem(withUNDangerousGoodsCode(UNDangerousGoodsCode(None)))),
            withItem(anItem(withUNDangerousGoodsCode(UNDangerousGoodsCode(Some("1234"))))),
            withItem(anItem())
          )
        )

        val result = controller.submit()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage

        theCacheModelUpdated.items.map(_.dangerousGoodsCode) mustBe Seq(
          Some(UNDangerousGoodsCode(None)),
          Some(UNDangerousGoodsCode(Some("1234"))),
          Some(UNDangerousGoodsCode(None))
        )
      }
    }

    "answer is No" should {
      "remove Carrier and Consignor Details from Parties and UN Code from Items in cache" in {
        withNewCaching(
          aDeclaration(
            withType(DeclarationType.CLEARANCE),
            withCarrierDetails(
              eori = Some(Eori("GB1234567890")),
              address = Some(
                Address(fullName = "Full Name", addressLine = "Address Line", townOrCity = "Town or City", postCode = "AB12 3CD", country = "UK")
              )
            ),
            withConsignorDetails(
              ConsignorDetails(
                EntityDetails(
                  Some(Eori("GB111222333")),
                  address = Some(
                    Address(fullName = "Full Name", addressLine = "Address Line", townOrCity = "Town or City", postCode = "AB12 3CD", country = "UK")
                  )
                )
              )
            ),
            withItem(anItem(withUNDangerousGoodsCode(UNDangerousGoodsCode(Some("1234")))))
          )
        )

        val correctForm = Json.toJson(IsExs(YesNoAnswers.no))

        controller.submit()(postRequest(correctForm)).futureValue

        val modelPassedToCache = theCacheModelUpdated
        modelPassedToCache.parties.isExs mustBe Some(IsExs(YesNoAnswers.no))
        modelPassedToCache.parties.carrierDetails mustBe None
        modelPassedToCache.parties.consignorDetails mustBe None
        modelPassedToCache.items.head.dangerousGoodsCode mustBe None
      }
    }

    "answer is No and declarant is not an exporter" should {
      "return 303 (SEE_OTHER) and redirect to Representative Agent page" in {
        withNewCaching(aDeclaration(withType(DeclarationType.CLEARANCE), withDeclarantIsExporter("No")))

        val correctForm = Json.toJson(IsExs(YesNoAnswers.no))

        val result = controller.submit()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe RepresentativeAgentController.displayPage
      }
    }

    "answer is No and declarant is an exporter" should {
      "return 303 (SEE_OTHER) and redirect to Consignee Details page" in {
        withNewCaching(aDeclaration(withType(DeclarationType.CLEARANCE), withDeclarantIsExporter()))

        val correctForm = Json.toJson(IsExs(YesNoAnswers.no))

        val result = controller.submit()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe ConsigneeDetailsController.displayPage
      }
    }
  }
}
