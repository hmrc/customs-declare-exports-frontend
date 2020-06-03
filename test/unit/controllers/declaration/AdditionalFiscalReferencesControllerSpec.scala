/*
 * Copyright 2020 HM Revenue & Customs
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

package unit.controllers.declaration

import controllers.declaration.{routes, AdditionalFiscalReferencesController}
import controllers.util.Remove
import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData}
import models.declaration.ExportItem
import models.{DeclarationType, ExportsDeclaration, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request, Result}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import unit.mock.{ErrorHandlerMocks, ItemActionMocks}
import views.html.declaration.additional_fiscal_references

import scala.concurrent.Future

class AdditionalFiscalReferencesControllerSpec extends ControllerSpec with ItemActionMocks with ErrorHandlerMocks {

  val additionalFiscalReferencesPage = mock[additional_fiscal_references]

  val controller = new AdditionalFiscalReferencesController(
    mockItemAction,
    mockErrorHandler,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    additionalFiscalReferencesPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    setupErrorHandler()
    authorizedUser()
    when(additionalFiscalReferencesPage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    reset(additionalFiscalReferencesPage)
  }

  def theResponseForm: Form[AdditionalFiscalReference] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[AdditionalFiscalReference]])
    verify(additionalFiscalReferencesPage).apply(any(), any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    val item = anItem()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))
    await(controller.displayPage(Mode.Normal, item.id)(request))
    theResponseForm
  }

  "Additional fiscal references controller" should {

    "return 200 (OK)" when {

      "display page method is invoked with empty cache" in {
        val item = anItem()
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))
        val result: Future[Result] = controller.displayPage(Mode.Normal, item.id)(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked with empty additional fiscal references" in {
        val itemCacheData = ExportItem("itemId", additionalFiscalReferencesData = None)
        val cachedData: ExportsDeclaration =
          aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(itemCacheData))
        withNewCaching(cachedData)

        val result: Future[Result] = controller.displayPage(Mode.Normal, itemCacheData.id)(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked with data in cache" in {

        val itemCacheData =
          ExportItem("itemId", additionalFiscalReferencesData = Some(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("PL", "12345")))))
        val cachedData: ExportsDeclaration =
          aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(itemCacheData))
        withNewCaching(cachedData)

        val result: Future[Result] = controller.displayPage(Mode.Normal, itemCacheData.id)(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user provide wrong action" in {
        val item = anItem()
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))

        val wrongAction: Seq[(String, String)] = Seq(("country", "PL"), ("reference", "12345"), ("WrongAction", ""))

        val result: Future[Result] =
          controller.saveReferences(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 400 (BAD_REQUEST) during adding" when {

      "user put incorrect data" in {
        val item = anItem()
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))

        val incorrectForm: Seq[(String, String)] = Seq(("country", "PL"), ("reference", "!@#$"), addActionUrlEncoded())

        val result: Future[Result] =
          controller.saveReferences(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user put duplicated item" in {

        val itemCacheData =
          ExportItem("itemId", additionalFiscalReferencesData = Some(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("PL", "12345")))))
        val cachedData: ExportsDeclaration =
          aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(itemCacheData))
        withNewCaching(cachedData)

        val duplicatedForm: Seq[(String, String)] = Seq(("country", "PL"), ("reference", "12345"), addActionUrlEncoded())

        val result: Future[Result] =
          controller.saveReferences(Mode.Normal, itemCacheData.id)(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reach maximum amount of items" in {

        val itemCacheData = ExportItem(
          "itemId",
          additionalFiscalReferencesData = Some(AdditionalFiscalReferencesData(Seq.fill(99)(AdditionalFiscalReference("PL", "12345"))))
        )
        val cachedData: ExportsDeclaration =
          aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(itemCacheData))
        withNewCaching(cachedData)

        val form: Seq[(String, String)] = Seq(("country", "PL"), ("reference", "54321"), addActionUrlEncoded())

        val result: Future[Result] =
          controller.saveReferences(Mode.Normal, itemCacheData.id)(postRequestAsFormUrlEncoded(form: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 400 (BAD_REQUEST) during saving" when {

      "user put incorrect data" in {
        val item = anItem()
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))

        val incorrectForm: Seq[(String, String)] =
          Seq(("country", "PL"), ("reference", "!@#$"), saveAndContinueActionUrlEncoded)

        val result: Future[Result] =
          controller.saveReferences(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user put duplicated item" in {

        val itemCacheData =
          ExportItem("itemId", additionalFiscalReferencesData = Some(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("PL", "12345")))))
        val cachedData: ExportsDeclaration =
          aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(itemCacheData))
        withNewCaching(cachedData)

        val duplicatedForm: Seq[(String, String)] =
          Seq(("country", "PL"), ("reference", "12345"), saveAndContinueActionUrlEncoded)

        val result: Future[Result] =
          controller.saveReferences(Mode.Normal, itemCacheData.id)(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reach maximum amount of items" in {

        val itemCacheData = ExportItem(
          "itemId",
          additionalFiscalReferencesData = Some(AdditionalFiscalReferencesData(Seq.fill(99)(AdditionalFiscalReference("PL", "12345"))))
        )
        val cachedData: ExportsDeclaration =
          aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(itemCacheData))
        withNewCaching(cachedData)

        val form: Seq[(String, String)] =
          Seq(("country", "PL"), ("reference", "54321"), saveAndContinueActionUrlEncoded)

        val result: Future[Result] =
          controller.saveReferences(Mode.Normal, itemCacheData.id)(postRequestAsFormUrlEncoded(form: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user correctly add new item" in {
        val item = anItem()
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))

        val correctForm: Seq[(String, String)] = Seq(("country", "PL"), ("reference", "12345"), addActionUrlEncoded())

        val result: Future[Result] =
          controller.saveReferences(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(SEE_OTHER)
      }

      "user save correct data" in {
        val item = anItem()
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))

        val correctForm: Seq[(String, String)] =
          Seq(("country", "PL"), ("reference", "12345"), saveAndContinueActionUrlEncoded)

        val result: Future[Result] =
          controller.saveReferences(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.CommodityDetailsController.displayPage(Mode.Normal, item.id)
      }

      "user save correct data without new item" in {
        val itemCacheData = ExportItem(
          "itemId",
          additionalFiscalReferencesData = Some(AdditionalFiscalReferencesData(Seq.fill(99)(AdditionalFiscalReference("PL", "12345"))))
        )
        val cachedData: ExportsDeclaration =
          aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(itemCacheData))
        withNewCaching(cachedData)

        val correctForm: (String, String) = saveAndContinueActionUrlEncoded

        val result: Future[Result] =
          controller.saveReferences(Mode.Normal, itemCacheData.id)(postRequestAsFormUrlEncoded(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.CommodityDetailsController.displayPage(Mode.Normal, "itemId")
      }

      "user remove existing item" in {

        val itemCacheData =
          ExportItem("itemId", additionalFiscalReferencesData = Some(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("PL", "12345")))))
        val cachedData: ExportsDeclaration =
          aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(itemCacheData))
        withNewCaching(cachedData)

        val removeForm: (String, String) = (Remove.toString, "0")

        val result: Future[Result] =
          controller.removeReference(Mode.Normal, itemCacheData.id, "12345")(postRequestAsFormUrlEncoded(removeForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.AdditionalFiscalReferencesController.displayPage(Mode.Normal, itemCacheData.id)
      }
    }
  }
}
