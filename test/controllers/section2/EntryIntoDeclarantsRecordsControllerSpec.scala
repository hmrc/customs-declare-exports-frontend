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
import controllers.section2.routes.PersonPresentingGoodsDetailsController
import controllers.general.routes.RootController
import controllers.section1.routes.DeclarantDetailsController
import forms.common.YesNoAnswer.{No, Yes, YesNoAnswers}
import forms.common.{Eori, YesNoAnswer}
import forms.section2.AuthorisationProcedureCodeChoice.Choice1040
import forms.section2.{EntryIntoDeclarantsRecords, PersonPresentingGoodsDetails}
import models.DeclarationType
import models.DeclarationType._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section2.entry_into_declarants_records

class EntryIntoDeclarantsRecordsControllerSpec extends ControllerSpec with AuditedControllerSpec with ScalaFutures {

  private val page = mock[entry_into_declarants_records]

  private val controller =
    new EntryIntoDeclarantsRecordsController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, mcc, page)(ec, auditService)

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

  private def theFormPassedToView: Form[YesNoAnswer] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(page).apply(formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  "EntryIntoDeclarantsRecordsController on displayOutcomePage" when {

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
          withNewCaching(aDeclarationAfter(request.cacheModel, withEntryIntoDeclarantsRecords()))

          controller.displayPage(getRequest()).futureValue

          theFormPassedToView.value mustBe defined
          theFormPassedToView.value.map(_.answer) mustBe Some(YesNoAnswers.yes)
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

  "EntryIntoDeclarantsRecordsController on submitForm" when {

    onClearance { request =>
      "everything works correctly" should {

        "return 303 (SEE_OTHER)" in {
          withNewCaching(request.cacheModel)
          val correctForm = Json.toJson(Map(EntryIntoDeclarantsRecords.fieldName -> YesNoAnswers.yes))

          val result = controller.submitForm()(postRequest(correctForm))

          status(result) mustBe SEE_OTHER
          verifyAudit()
        }

        "call Cache to update it" in {
          withNewCaching(request.cacheModel)
          val correctForm = Json.toJson(Map(EntryIntoDeclarantsRecords.fieldName -> YesNoAnswers.yes))

          controller.submitForm()(postRequest(correctForm)).futureValue

          theCacheModelUpdated.parties.isEntryIntoDeclarantsRecords mustBe Yes
          verifyAudit()
        }

        "call Navigator" in {
          withNewCaching(request.cacheModel)
          val correctForm = Json.toJson(Map(EntryIntoDeclarantsRecords.fieldName -> YesNoAnswers.yes))

          controller.submitForm()(postRequest(correctForm)).futureValue

          verify(navigator).continueTo(any())(any())
        }
      }

      "provided with 'Yes' answer" should {

        "update Cache with PersonPresentingGoodsDetails left unchanged" in {
          val cachedParties = request.cacheModel.parties.copy(personPresentingGoodsDetails = Some(PersonPresentingGoodsDetails(Eori("GB1234567890"))))
          withNewCaching(request.cacheModel.copy(parties = cachedParties))
          val correctForm = Json.toJson(Map(EntryIntoDeclarantsRecords.fieldName -> YesNoAnswers.yes))

          controller.submitForm()(postRequest(correctForm)).futureValue

          val modelPassedToCache = theCacheModelUpdated
          modelPassedToCache.parties.isEntryIntoDeclarantsRecords mustBe Yes
          modelPassedToCache.parties.personPresentingGoodsDetails mustBe Some(PersonPresentingGoodsDetails(Eori("GB1234567890")))
          verifyAudit()
        }

        "update Cache with AuthorisationProcedureCodeChoice left unchanged" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withAuthorisationProcedureCodeChoice(Choice1040)))
          val correctForm = Json.obj(EntryIntoDeclarantsRecords.fieldName -> YesNoAnswers.yes)

          controller.submitForm()(postRequest(correctForm)).futureValue

          val modelPassedToCache = theCacheModelUpdated
          modelPassedToCache.parties.isEntryIntoDeclarantsRecords mustBe Yes
          modelPassedToCache.parties.authorisationProcedureCodeChoice mustBe Choice1040
          verifyAudit()
        }

        "redirect to Person Presenting the Goods page" in {
          withNewCaching(request.cacheModel)
          val correctForm = Json.toJson(Map(EntryIntoDeclarantsRecords.fieldName -> YesNoAnswers.yes))

          controller.submitForm()(postRequest(correctForm)).futureValue

          thePageNavigatedTo mustBe PersonPresentingGoodsDetailsController.displayPage
        }
      }

      "provided with 'No' answer" should {

        "update Cache with PersonPresentingGoodsDetails set to None" in {
          val cachedParties = request.cacheModel.parties.copy(personPresentingGoodsDetails = Some(PersonPresentingGoodsDetails(Eori("GB1234567890"))))
          withNewCaching(request.cacheModel.copy(parties = cachedParties))
          val correctForm = Json.toJson(Map(EntryIntoDeclarantsRecords.fieldName -> YesNoAnswers.no))

          controller.submitForm()(postRequest(correctForm)).futureValue

          val modelPassedToCache = theCacheModelUpdated
          modelPassedToCache.parties.isEntryIntoDeclarantsRecords mustBe No
          modelPassedToCache.parties.personPresentingGoodsDetails mustBe None
          verifyAudit()
        }

        "update Cache with AuthorisationProcedureCodeChoice set to None" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withAuthorisationProcedureCodeChoice(Choice1040)))
          val correctForm = Json.obj(EntryIntoDeclarantsRecords.fieldName -> YesNoAnswers.no)

          controller.submitForm()(postRequest(correctForm)).futureValue

          val modelPassedToCache = theCacheModelUpdated
          modelPassedToCache.parties.isEntryIntoDeclarantsRecords mustBe No
          modelPassedToCache.parties.authorisationProcedureCodeChoice mustBe None
          verifyAudit()
        }

        "redirect to Declarant Details page" in {
          withNewCaching(request.cacheModel)
          val correctForm = Json.toJson(Map(EntryIntoDeclarantsRecords.fieldName -> YesNoAnswers.no))

          controller.submitForm()(postRequest(correctForm)).futureValue

          thePageNavigatedTo mustBe DeclarantDetailsController.displayPage
        }
      }

      "provided with incorrect data" should {
        "return 400 (BAD_REQUEST)" in {
          withNewCaching(request.cacheModel)
          val incorrectForm = Json.toJson(Map(EntryIntoDeclarantsRecords.fieldName -> "Incorrect"))

          val result = controller.submitForm()(postRequest(incorrectForm))

          status(result) mustBe BAD_REQUEST
          verifyNoAudit()
        }
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
      "return 303 (SEE_OTHER)" in {
        withNewCaching(request.cacheModel)
        val correctForm = Json.toJson(Yes.value)

        val result = controller.submitForm()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        verifyNoAudit()
      }

      "redirect to start page" in {
        withNewCaching(request.cacheModel)
        val correctForm = Json.toJson(Yes.value)

        val result = controller.submitForm()(postRequest(correctForm))

        redirectLocation(result) mustBe Some(RootController.displayPage.url)
      }
    }
  }
}
