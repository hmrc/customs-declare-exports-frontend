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

import base.{AuditedControllerSpec, ControllerSpec, Injector, TestHelper}
import controllers.section2.routes.AdditionalActorsSummaryController
import forms.common.Eori
import forms.section2.AdditionalActor
import models.DeclarationType
import models.declaration.AdditionalActors
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section2.additionalActors.additional_actors_add

class AdditionalActorsAddControllerSpec extends ControllerSpec with AuditedControllerSpec with Injector {

  val declarationAdditionalActorsPage = mock[additional_actors_add]

  val controller =
    new AdditionalActorsAddController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, mcc, declarationAdditionalActorsPage)(
      ec,
      auditService
    )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    setupErrorHandler()
    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))

    when(declarationAdditionalActorsPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(declarationAdditionalActorsPage)
    super.afterEach()
  }

  def theResponseForm: Form[AdditionalActor] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[AdditionalActor]])
    verify(declarationAdditionalActorsPage).apply(formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(request))
    theResponseForm
  }

  val eori = "GB12345678912345"
  val additionalActor = AdditionalActor(Some(Eori(eori)), Some("CS"))
  val declarationWithActor =
    aDeclaration(withAdditionalActors(additionalActor))

  val maxAmountOfItems = aDeclaration(withAdditionalActors(AdditionalActors(Seq.fill(AdditionalActors.maxNumberOfActors)(additionalActor))))

  "Declaration Additional Actors controller" should {

    "return 200 (OK)" when {

      "display page method is invoked with empty cache" in {
        val result = controller.displayPage(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked with data in cache" in {
        withNewCaching(declarationWithActor)

        val result = controller.displayPage(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {
      "user provide wrong action" in {
        val wrongAction = Seq(("eori", "GB123456"), ("partyType", "CS"), ("WrongAction", ""))

        val result = controller.saveForm()(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 400 (BAD_REQUEST) during saving" when {

      "user put incorrect data" in {
        val longerEori = TestHelper.createRandomAlphanumericString(18)
        val wrongAction = Seq(("eoriCS", longerEori), ("partyType", "CS"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm()(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) must be(BAD_REQUEST)
        verifyNoAudit()
      }

      "user put duplicated item" in {
        withNewCaching(declarationWithActor)

        val duplication = Seq(("eoriCS", eori), ("partyType", "CS"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm()(postRequestAsFormUrlEncoded(duplication: _*))

        status(result) must be(BAD_REQUEST)
        verifyNoAudit()
      }

      "user reach maximum amount of items" in {
        withNewCaching(maxAmountOfItems)

        val correctForm = Seq(("eoriCS", "GB123456"), ("partyType", "CS"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm()(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(BAD_REQUEST)
        verifyNoAudit()
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user add correct consolidator" in {
        val correctForm = Seq(("eoriCS", eori), ("partyType", "CS"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm()(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe AdditionalActorsSummaryController.displayPage
        verifyAudit()
      }

      "user add correct manufacturer" in {
        val correctForm = Seq(("eoriMF", eori), ("partyType", "MF"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm()(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe AdditionalActorsSummaryController.displayPage
        verifyAudit()
      }

      "user add correct freight forwarder" in {
        val correctForm = Seq(("eoriFW", eori), ("partyType", "FW"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm()(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe AdditionalActorsSummaryController.displayPage
        verifyAudit()
      }

      "user add correct warehouse keeper" in {
        val correctForm = Seq(("eoriWH", eori), ("partyType", "WH"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm()(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe AdditionalActorsSummaryController.displayPage
        verifyAudit()
      }
    }
  }
}
