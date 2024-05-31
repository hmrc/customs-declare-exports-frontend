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

import base.ControllerWithoutFormSpec
import controllers.declaration.routes.{ItemsSummaryController, ProcedureCodesController, SectionSummaryController}
import controllers.helpers.SequenceIdHelper.valueOfEso
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import models.DeclarationType.CLEARANCE
import models.declaration.{CommodityMeasure, ExportItem}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{GivenWhenThen, OptionValues}
import play.api.data.{Form, FormError}
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.declarationitems.{items_add_item, items_summary}

import scala.concurrent.Future

class ItemsSummaryControllerSpec extends ControllerWithoutFormSpec with OptionValues with ScalaFutures with GivenWhenThen {

  private val addItemPage = mock[items_add_item]
  private val itemsSummaryPage = mock[items_summary]

  private val controller = new ItemsSummaryController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    mcc,
    addItemPage,
    itemsSummaryPage
  )(ec)

  private val parentDeclarationId = "parentDecId"
  private val parentDeclaration = aDeclaration(withId(parentDeclarationId))

  private val exportItem: ExportItem = anItem(
    withSequenceId(1),
    withItemId("Some Id"),
    withProcedureCodes(),
    withFiscalInformation(),
    withAdditionalFiscalReferenceData(),
    withStatisticalValue(),
    withPackageInformation(),
    withAdditionalInformation("code", "description"),
    withCommodityMeasure(CommodityMeasure(None, Some(true), Some("100"), Some("100")))
  )

  private def formPassedToItemsSummaryView: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(itemsSummaryPage).apply(captor.capture(), any(), any())(any(), any())
    captor.getValue
  }

  private def itemsPassedToItemsSummaryView: List[ExportItem] = {
    val captor = ArgumentCaptor.forClass(classOf[List[ExportItem]])
    verify(itemsSummaryPage).apply(any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def itemsErrorsPassedToItemsSummaryView: Seq[FormError] = {
    val captor = ArgumentCaptor.forClass(classOf[Seq[FormError]])
    verify(itemsSummaryPage).apply(any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    setupErrorHandler()
    authorizedUser()

    when(addItemPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)
    when(itemsSummaryPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any())).thenReturn(Future.successful(Some(parentDeclaration)))
  }

  override protected def afterEach(): Unit = {
    reset(addItemPage, itemsSummaryPage, mockExportsCacheService, mockCustomsDeclareExportsConnector)
    super.afterEach()
  }

  "displayAddItemPage" should {

    onEveryDeclarationJourney() { request =>
      "call cache" in {
        withNewCaching(aDeclaration(withType(request.declarationType)))

        val result = controller.displayAddItemPage()(getRequest())
        status(result) mustBe OK
        verify(mockExportsCacheService).get(anyString())(any())
      }

      "return 200 (OK)" when {
        "there is no item in cache" in {
          withNewCaching(aDeclaration(withType(request.declarationType)))

          val result = controller.displayAddItemPage()(getRequest())
          status(result) mustBe OK
          verify(addItemPage).apply()(any(), any())
        }
      }

      "return 303 (SEE_OTHER) and redirect to displayItemsSummaryPage" when {
        "there are items in cache" in {
          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem))
          withNewCaching(cachedData)

          val result = controller.displayAddItemPage()(getRequest())

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo mustBe ItemsSummaryController.displayItemsSummaryPage
        }
      }
    }
  }

  "addFirstItem" should {

    onEveryDeclarationJourney() { request =>
      "call Navigator" in {
        withNewCaching(aDeclaration(withType(request.declarationType)))

        val result = controller.addFirstItem()(postRequest(Json.obj()))
        status(result) mustBe SEE_OTHER

        verify(navigator).continueTo(any())(any())
      }

      "return 303 (SEE_OTHER) and redirect to Procedure Codes page" in {
        withNewCaching(aDeclaration(withType(request.declarationType)))

        val result = controller.addFirstItem()(postRequest(Json.obj()))
        status(result) mustBe SEE_OTHER
        val itemId = theCacheModelUpdated.items.last.id
        thePageNavigatedTo mustBe ProcedureCodesController.displayPage(itemId)

        val declaration = theCacheModelUpdated
        declaration.items.size mustBe 1

        And("the max sequence id for export items is updated")
        valueOfEso[ExportItem](declaration).value mustBe 1
      }
    }
  }

  "displayItemsSummaryPage" should {
    onEveryDeclarationJourney() { request =>
      "call cache" in {
        withNewCaching(aDeclaration(withType(request.declarationType)))

        val result = controller.displayItemsSummaryPage()(getRequest())
        status(result) mustBe SEE_OTHER

        verify(mockExportsCacheService).get(anyString())(any())
      }

      "return 200 (OK)" when {
        "there are items in cache" in {
          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem))
          withNewCaching(cachedData)

          val result = controller.displayItemsSummaryPage()(getRequest())
          status(result) mustBe OK

          verify(itemsSummaryPage).apply(any(), any(), any())(any(), any())
          itemsPassedToItemsSummaryView mustBe Seq(exportItem)
        }
      }

      "return 303 (SEE_OTHER) and redirect to displayAddItemPage" when {
        "there is no item in cache" in {
          withNewCaching(aDeclaration(withType(request.declarationType)))

          val result = controller.displayItemsSummaryPage()(getRequest())

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo mustBe ItemsSummaryController.displayAddItemPage
        }
      }

      "remove un-used item" when {
        "there is unused item in cache" in {
          val emptyItem = anItem()
          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem), withItem(emptyItem))
          withNewCaching(cachedData)

          val result = controller.displayItemsSummaryPage()(getRequest())

          status(result) mustBe OK
          verify(itemsSummaryPage).apply(any(), any(), any())(any(), any())
          itemsPassedToItemsSummaryView mustBe Seq(exportItem)
        }
      }
    }
  }

  "submit" when {

    onEveryDeclarationJourney() { request =>
      "user wants to add another item" should {

        "call Navigator" in {
          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem))
          withNewCaching(cachedData)
          val answerForm = Json.obj("yesNo" -> YesNoAnswers.yes)

          val result = controller.submit()(postRequest(answerForm))
          status(result) mustBe SEE_OTHER

          verify(navigator).continueTo(any())(any())
        }

        "return 303 (SEE_OTHER) and redirect to Procedure Codes page" in {
          val cachedData = aDeclaration(withType(request.declarationType), withItem(anItem()))
          withNewCaching(cachedData)
          val answerForm = Json.obj("yesNo" -> YesNoAnswers.yes)

          val result = controller.submit()(postRequest(answerForm))
          status(result) mustBe SEE_OTHER

          if (request.isType(CLEARANCE))
            thePageNavigatedTo mustBe SectionSummaryController.displayPage(5)
          else {
            val declaration = theCacheModelUpdated
            thePageNavigatedTo mustBe ProcedureCodesController.displayPage(declaration.items.last.id)

            And("max sequence id is updated")
            valueOfEso[ExportItem](declaration).value mustBe 0
          }

          verify(navigator).continueTo(any())(any())
        }
      }

      "user does not want to add another item" should {
        "return 303 (SEE_OTHER) and redirect to next page" in {
          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem))
          withNewCaching(cachedData)
          val answerForm = Json.obj("yesNo" -> YesNoAnswers.no)

          val result = controller.submit()(postRequest(answerForm))

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo mustBe SectionSummaryController.displayPage(5)

          verify(navigator).continueTo(any())(any())
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "there is no answer from user" in {
          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem))
          withNewCaching(cachedData)

          val result = controller.submit()(postRequest(Json.obj()))

          if (request.isType(CLEARANCE)) {
            status(result) mustBe SEE_OTHER
          } else {
            status(result) mustBe BAD_REQUEST
            formPassedToItemsSummaryView.errors mustNot be(empty)
          }
        }

        "there is incomplete item in the cache" in {
          val cachedData = aDeclaration(withType(request.declarationType), withItem(anItem(withItemId("id"))))
          withNewCaching(cachedData)
          val answerForm = Json.obj("yesNo" -> YesNoAnswers.no)

          val result = controller.submit()(postRequest(answerForm))

          if (request.isType(CLEARANCE)) {
            status(result) mustBe SEE_OTHER
          } else {
            status(result) mustBe BAD_REQUEST
            itemsErrorsPassedToItemsSummaryView mustNot be(empty)
          }
        }
      }
      "user does not want to add another item" should {
        "return 303 (SEE_OTHER) and redirect to mini CYA page for Items" in {
          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem))
          withNewCaching(cachedData)
          val answerForm = Json.obj("yesNo" -> YesNoAnswers.no)

          val result = controller.submit()(postRequest(answerForm))

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo mustBe SectionSummaryController.displayPage(5)
        }
      }
    }
  }
}
