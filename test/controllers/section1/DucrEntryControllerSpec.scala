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

package controllers.section1

import base.{AuditedControllerSpec, ControllerSpec}
import controllers.actions.AmendmentDraftFilterSpec
import controllers.section1.routes.LocalReferenceNumberController
import forms.section1.Ducr
import models.DeclarationType._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.GivenWhenThen
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section1.ducr_entry

class DucrEntryControllerSpec extends ControllerSpec with AuditedControllerSpec with AmendmentDraftFilterSpec with GivenWhenThen {

  private val ducrEntryPage = mock[ducr_entry]

  val controller =
    new DucrEntryController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, mcc, ducrEntryPage)(ec, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(ducrEntryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    reset(ducrEntryPage)
  }

  def nextPageOnTypes: Seq[NextPageOnType] =
    allDeclarationTypesExcluding(SUPPLEMENTARY).map(NextPageOnType(_, LocalReferenceNumberController.displayPage))

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(request))
    theResponseForm
  }

  def theResponseForm: Form[Ducr] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Ducr]])
    verify(ducrEntryPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  "DucrEntryController on displayOutcomePage" should {

    onJourney(STANDARD, OCCASIONAL, SIMPLIFIED, CLEARANCE) { request =>
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

  "DucrEntryController on submitForm" should {
    onJourney(STANDARD, OCCASIONAL, SIMPLIFIED, CLEARANCE) { request =>
      "return 400 (BAD_REQUEST)" when {
        "user enters incorrect data" in {
          withNewCaching(request.cacheModel)
          val incorrectForm = Json.toJson(Ducr("1234"))

          val result = controller.submitForm()(postRequest(incorrectForm))
          status(result) must be(BAD_REQUEST)
          verifyNoAudit()
        }
      }

      "change to uppercase any lowercase letter entered in the DUCR field" in {
        withNewCaching(request.cacheModel)

        val ducr = "9gb123456664559-1abc"
        val correctForm = Json.toJson(Ducr(ducr))
        val result = controller.submitForm()(postRequest(correctForm))

        And("return 303 (SEE_OTHER)")
        await(result) mustBe aRedirectToTheNextPage

        val declaration = theCacheModelUpdated
        declaration.consignmentReferences.head.ducr.get.ducr mustBe ducr.toUpperCase
        verifyAudit()
      }

      "return 303 (SEE_OTHER) and redirect to 'Lrn' page" in {
        val correctForm = Json.toJson(Ducr(DUCR))
        withNewCaching(request.cacheModel)

        val result = controller.submitForm()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe LocalReferenceNumberController.displayPage
        verifyAudit()
      }
    }
  }
}
