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
import controllers.declaration.ConsignmentReferencesController
import forms.declaration.ConsignmentReferences
import forms.{Ducr, Lrn}
import models.DeclarationType.{OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.Mode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.consignment_references

class ConsignmentReferencesControllerSpec extends ControllerSpec {

  val consignmentReferencesPage = mock[consignment_references]

  val controller = new ConsignmentReferencesController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    consignmentReferencesPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(consignmentReferencesPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    reset(consignmentReferencesPage)
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal)(request))
    theResponseForm
  }

  def theResponseForm: Form[ConsignmentReferences] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[ConsignmentReferences]])
    verify(consignmentReferencesPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  "ConsignmentReferencesController on displayPage" should {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {

          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
        }

        "display page method is invoked and cache contains data" in {

          withNewCaching(aDeclaration(withConsignmentReferences()))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
        }
      }
    }
  }

  "ConsignmentReferencesController on displayPage" should {

    onEveryDeclarationJourney() { request =>
      "return 400 (BAD_REQUEST)" in {

        withNewCaching(request.cacheModel)
        val incorrectForm = Json.toJson(ConsignmentReferences(Ducr("1234"), Lrn("")))

        val result = controller.submitConsignmentReferences(Mode.Normal)(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
      "return 303 (SEE_OTHER) and redirect to Declarant Exporter page" in {

        withNewCaching(request.cacheModel)
        val correctForm = Json.toJson(ConsignmentReferences(Ducr(DUCR), LRN))

        val result = controller.submitConsignmentReferences(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DeclarantExporterController.displayPage()
      }
    }

    onClearance { request =>
      "return 303 (SEE_OTHER) and redirect to Entry into Declarant's Records page" in {

        withNewCaching(request.cacheModel)
        val correctForm = Json.toJson(ConsignmentReferences(Ducr(DUCR), LRN))

        val result = controller.submitConsignmentReferences(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.EntryIntoDeclarantsRecordsController.displayPage()
      }
    }
  }
}
