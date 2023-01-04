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
import controllers.declaration.routes.{DeclarantDetailsController, PersonPresentingGoodsDetailsController}
import controllers.routes.RootController
import forms.common.YesNoAnswer.{No, Yes, YesNoAnswers}
import forms.common.{Eori, YesNoAnswer}
import forms.declaration.AuthorisationProcedureCodeChoice.Choice1040
import forms.declaration.{EntryIntoDeclarantsRecords, PersonPresentingGoodsDetails}
import models.DeclarationType._
import models.{DeclarationType, ExportsDeclaration}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.entry_into_declarants_records

class EntryIntoDeclarantsRecordsControllerSpec extends ControllerSpec with ScalaFutures {

  private val page = mock[entry_into_declarants_records]

  private val controller = new EntryIntoDeclarantsRecordsController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    page
  )(ec)

  override def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(page.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach(): Unit = {
    reset(page)
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

  private def theModelPassedToCacheUpdate: ExportsDeclaration = {
    val modelCaptor = ArgumentCaptor.forClass(classOf[ExportsDeclaration])
    verify(mockExportsCacheService).update(modelCaptor.capture())(any())
    modelCaptor.getValue
  }

  "EntryIntoDeclarantsRecordsController on displayPage" when {

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
        }

        "call Cache to update it" in {
          withNewCaching(request.cacheModel)
          val correctForm = Json.toJson(Map(EntryIntoDeclarantsRecords.fieldName -> YesNoAnswers.yes))

          controller.submitForm()(postRequest(correctForm)).futureValue

          theModelPassedToCacheUpdate.parties.isEntryIntoDeclarantsRecords mustBe Yes
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

          val modelPassedToCache = theModelPassedToCacheUpdate
          modelPassedToCache.parties.isEntryIntoDeclarantsRecords mustBe Yes
          modelPassedToCache.parties.personPresentingGoodsDetails mustBe Some(PersonPresentingGoodsDetails(Eori("GB1234567890")))
        }

        "update Cache with AuthorisationProcedureCodeChoice left unchanged" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withAuthorisationProcedureCodeChoice(Choice1040)))
          val correctForm = Json.obj(EntryIntoDeclarantsRecords.fieldName -> YesNoAnswers.yes)

          controller.submitForm()(postRequest(correctForm)).futureValue

          val modelPassedToCache = theModelPassedToCacheUpdate
          modelPassedToCache.parties.isEntryIntoDeclarantsRecords mustBe Yes
          modelPassedToCache.parties.authorisationProcedureCodeChoice mustBe Choice1040
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

          val modelPassedToCache = theModelPassedToCacheUpdate
          modelPassedToCache.parties.isEntryIntoDeclarantsRecords mustBe No
          modelPassedToCache.parties.personPresentingGoodsDetails mustBe None
        }

        "update Cache with AuthorisationProcedureCodeChoice set to None" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withAuthorisationProcedureCodeChoice(Choice1040)))
          val correctForm = Json.obj(EntryIntoDeclarantsRecords.fieldName -> YesNoAnswers.no)

          controller.submitForm()(postRequest(correctForm)).futureValue

          val modelPassedToCache = theModelPassedToCacheUpdate
          modelPassedToCache.parties.isEntryIntoDeclarantsRecords mustBe No
          modelPassedToCache.parties.authorisationProcedureCodeChoice mustBe None
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
        }
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
      "return 303 (SEE_OTHER)" in {
        withNewCaching(request.cacheModel)
        val correctForm = Json.toJson(Yes.value)

        val result = controller.submitForm()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
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
