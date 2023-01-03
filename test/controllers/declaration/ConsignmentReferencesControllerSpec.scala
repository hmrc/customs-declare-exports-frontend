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
import base.ExportsTestData.eidrDateStamp
import forms.declaration.ConsignmentReferences
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.{SUPPLEMENTARY_EIDR, SUPPLEMENTARY_SIMPLIFIED}
import forms.{Ducr, Lrn, LrnValidator}
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.GivenWhenThen
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.consignment_references

import scala.concurrent.Future

class ConsignmentReferencesControllerSpec extends ControllerSpec with GivenWhenThen {

  private val lrnValidator = mock[LrnValidator]
  private val consignmentReferencesPage = mock[consignment_references]

  val controller = new ConsignmentReferencesController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    lrnValidator,
    navigator,
    stubMessagesControllerComponents(),
    consignmentReferencesPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(lrnValidator.hasBeenSubmittedInThePast48Hours(any[Lrn])(any(), any())).thenReturn(Future.successful(false))
    when(consignmentReferencesPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    reset(lrnValidator, consignmentReferencesPage)
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(request))
    theResponseForm
  }

  def theResponseForm: Form[ConsignmentReferences] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[ConsignmentReferences]])
    verify(consignmentReferencesPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  "ConsignmentReferencesController on displayPage" should {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(getRequest())
          status(result) must be(OK)
        }

        "display page method is invoked and cache contains data" in {
          withNewCaching(aDeclaration(withConsignmentReferences()))

          val result = controller.displayPage(getRequest())
          status(result) must be(OK)
        }
      }
    }
  }

  "ConsignmentReferencesController on submitConsignmentReferences" should {

    onEveryDeclarationJourney() { request =>
      "return 400 (BAD_REQUEST)" when {

        "user enters incorrect data" in {
          withNewCaching(request.cacheModel)
          val incorrectForm = Json.toJson(ConsignmentReferences(Some(Ducr("1234")), Some(Lrn(""))))

          val result = controller.submitConsignmentReferences()(postRequest(incorrectForm))
          status(result) must be(BAD_REQUEST)
        }

        "LrnValidator returns false" in {
          when(lrnValidator.hasBeenSubmittedInThePast48Hours(any[Lrn])(any(), any())).thenReturn(Future.successful(true))
          withNewCaching(request.cacheModel)
          val correctForm = Json.toJson(ConsignmentReferences(Some(Ducr(DUCR)), Some(LRN)))

          val result = controller.submitConsignmentReferences()(postRequest(correctForm))
          status(result) must be(BAD_REQUEST)
        }
      }

      "change to uppercase any lowercase letter entered in the DUCR field" in {
        withNewCaching(request.cacheModel)

        val ducr = "9gb123456664559-1abc"
        val correctForm = Json.toJson(ConsignmentReferences(Some(Ducr(ducr)), Some(LRN)))
        val result = controller.submitConsignmentReferences()(postRequest(correctForm))

        And("return 303 (SEE_OTHER)")
        await(result) mustBe aRedirectToTheNextPage

        val declaration = theCacheModelUpdated
        declaration.consignmentReferences.head.ducr.get.ducr mustBe ducr.toUpperCase
      }
    }

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { request =>
      val correctForm = Json.toJson(ConsignmentReferences(Some(Ducr(DUCR)), Some(LRN)))

      "return 303 (SEE_OTHER) and redirect to 'Link DUCR to MUCR' page" in {
        withNewCaching(request.cacheModel)

        val result = controller.submitConsignmentReferences()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.LinkDucrToMucrController.displayPage
      }
    }

    onJourney(SUPPLEMENTARY) { req =>
      "return 303 (SEE_OTHER) and redirect to 'Link DUCR to MUCR' page" when {

        "for SUPPLEMENTARY_SIMPLIFIED" in {
          val request = journeyRequest(aDeclaration(withType(req.declarationType), withAdditionalDeclarationType(SUPPLEMENTARY_SIMPLIFIED)))
          withNewCaching(request.cacheModel)
          val correctForm = Json.toJson(ConsignmentReferences(Some(Ducr(DUCR)), Some(LRN), Some(MRN)))

          val result = controller.submitConsignmentReferences()(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.DeclarantExporterController.displayPage
        }

        "return 303 (SEE_OTHER) and redirect to 'Link DUCR to MUCR' page for SUPPLEMENTARY_EIDR" in {
          val request = journeyRequest(aDeclaration(withType(req.declarationType), withAdditionalDeclarationType(SUPPLEMENTARY_EIDR)))
          withNewCaching(request.cacheModel)
          val correctForm = Json.toJson(ConsignmentReferences(Some(Ducr(DUCR)), Some(LRN), None, Some(eidrDateStamp)))

          val result = controller.submitConsignmentReferences()(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.DeclarantExporterController.displayPage
        }
      }
    }
  }
}
