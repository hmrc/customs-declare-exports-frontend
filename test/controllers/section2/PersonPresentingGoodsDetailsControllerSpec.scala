/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.section2

import base.{AuditedControllerSpec, ControllerSpec}
import controllers.section2.routes.ExporterEoriNumberController
import controllers.general.routes.RootController
import forms.common.Eori
import forms.section2.PersonPresentingGoodsDetails
import models.DeclarationType
import models.DeclarationType.{OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section2.person_presenting_goods_details

class PersonPresentingGoodsDetailsControllerSpec extends ControllerSpec with AuditedControllerSpec with ScalaFutures {

  private val testEori = "GB1234567890000"

  private val page = mock[person_presenting_goods_details]

  private val controller =
    new PersonPresentingGoodsDetailsController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, mcc, page)(ec, auditService)

  override def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(page.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach(): Unit = {
    reset(page, auditService)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withType(DeclarationType.CLEARANCE)))
    await(controller.displayPage(request))
    theFormPassedToView
  }

  def theFormPassedToView: Form[PersonPresentingGoodsDetails] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[PersonPresentingGoodsDetails]])
    verify(page).apply(formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  "PersonPresentingGoodsDetailsController on displayOutcomePage" when {

    onClearance { request =>
      "everything works correctly" should {

        "return 200 (OK)" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(getRequest())

          status(result) mustBe OK
        }

        "call ExportsCacheService" in {
          withNewCaching(request.cacheModel)

          controller.displayPage(getRequest()).futureValue

          verify(mockExportsCacheService).get(meq(existingDeclarationId))(any())
        }

        "call page view, passing form with data from cache" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withPersonPresentingGoods(Some(Eori(testEori)))))

          controller.displayPage(getRequest()).futureValue

          theFormPassedToView.value mustBe defined
          theFormPassedToView.value.map(_.eori) mustBe Some(Eori(testEori))
        }
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
      "return 303 (SEE_OTHER)" in {
        withNewCaching(request.cacheModel)

        val result = controller.displayPage(getRequest())

        status(result) mustBe SEE_OTHER
      }

      "redirect to start page" in {
        withNewCaching(request.cacheModel)

        val result = controller.displayPage(getRequest())

        redirectLocation(result) mustBe Some(RootController.displayPage.url)
      }
    }
  }

  "PersonPresentingGoodsDetailsController on submitForm" when {

    onClearance { request =>
      "everything works correctly" should {

        "return 303 (SEE_OTHER)" in {
          withNewCaching(request.cacheModel)
          val correctForm = Json.toJson(Map(PersonPresentingGoodsDetails.fieldName -> testEori))

          val result = controller.submitForm()(postRequest(correctForm))

          status(result) mustBe SEE_OTHER
          verifyAudit()
        }

        "redirect to Exporter Details page" in {
          withNewCaching(request.cacheModel)
          val correctForm = Json.toJson(Map(PersonPresentingGoodsDetails.fieldName -> testEori))

          controller.submitForm()(postRequest(correctForm)).futureValue

          thePageNavigatedTo mustBe ExporterEoriNumberController.displayPage
        }

        "call Cache to update it" in {
          withNewCaching(request.cacheModel)
          val correctForm = Json.toJson(Map(PersonPresentingGoodsDetails.fieldName -> testEori))

          controller.submitForm()(postRequest(correctForm)).futureValue

          theCacheModelUpdated.parties.personPresentingGoodsDetails mustBe Some(PersonPresentingGoodsDetails(Eori(testEori)))
          verifyAudit()
        }

        "call Navigator" in {
          withNewCaching(request.cacheModel)
          val correctForm = Json.toJson(Map(PersonPresentingGoodsDetails.fieldName -> testEori))

          controller.submitForm()(postRequest(correctForm)).futureValue

          verify(navigator).continueTo(any())(any())
        }
      }

      "provided with incorrect data" should {
        "return 400 (BAD_REQUEST)" in {
          withNewCaching(request.cacheModel)
          val correctForm = Json.toJson(Map(PersonPresentingGoodsDetails.fieldName -> "Incorrect!@#"))

          val result = controller.submitForm()(postRequest(correctForm))

          status(result) mustBe BAD_REQUEST
        }
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
      "return 303 (SEE_OTHER)" in {
        withNewCaching(request.cacheModel)
        val correctForm = Json.toJson(Map(PersonPresentingGoodsDetails.fieldName -> testEori))

        val result = controller.submitForm()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        verifyNoAudit()
      }

      "redirect to start page" in {
        withNewCaching(request.cacheModel)
        val correctForm = Json.toJson(Map(PersonPresentingGoodsDetails.fieldName -> testEori))

        val result = controller.submitForm()(postRequest(correctForm))

        redirectLocation(result) mustBe Some(RootController.displayPage.url)
      }
    }
  }
}
