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
import forms.Choice
import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData}
import models.declaration.ExportItem
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.mvc.Result
import play.api.test.Helpers._
import unit.base.ControllerSpec
import unit.mock.{ErrorHandlerMocks, ItemActionMocks}
import views.html.declaration.additional_fiscal_references

import scala.concurrent.Future

class AdditionalFiscalReferencesControllerSpec extends ControllerSpec with ItemActionMocks with ErrorHandlerMocks {

  trait SetUp {

    val additionalFiscalReferencesPage = new additional_fiscal_references(mainTemplate)

    val controller = new AdditionalFiscalReferencesController(
      mockItemAction,
      mockErrorHandler,
      mockExportsCacheService,
      navigator,
      stubMessagesControllerComponents(),
      additionalFiscalReferencesPage
    )(ec)

    setupErrorHandler()
    authorizedUser()

  }

  "Additional fiscal references controller" should {

    "return 200 (OK)" when {

      "display page method is invoked with empty cache" in new SetUp {
        val item = anItem()
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))
        val result: Future[Result] = controller.displayPage(Mode.Normal, item.id)(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked with empty additional fiscal references" in new SetUp {
        val itemCacheData = ExportItem("itemId", additionalFiscalReferencesData = None)
        val cachedData: ExportsDeclaration =
          aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(itemCacheData))
        withNewCaching(cachedData)

        val result: Future[Result] = controller.displayPage(Mode.Normal, itemCacheData.id)(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked with data in cache" in new SetUp {

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

      "user provide wrong action" in new SetUp {
        val item = anItem()
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))

        val wrongAction: Seq[(String, String)] = Seq(("country", "PL"), ("reference", "12345"), ("WrongAction", ""))

        val result: Future[Result] =
          controller.saveReferences(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 400 (BAD_REQUEST) during adding" when {

      "user put incorrect data" in new SetUp {
        val item = anItem()
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))

        val incorrectForm: Seq[(String, String)] = Seq(("country", "PL"), ("reference", "!@#$"), addActionUrlEncoded())

        val result: Future[Result] =
          controller.saveReferences(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user put duplicated item" in new SetUp {

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

      "user reach maximum amount of items" in new SetUp {

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

      "user put incorrect data" in new SetUp {
        val item = anItem()
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))

        val incorrectForm: Seq[(String, String)] =
          Seq(("country", "PL"), ("reference", "!@#$"), saveAndContinueActionUrlEncoded)

        val result: Future[Result] =
          controller.saveReferences(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user put duplicated item" in new SetUp {

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

      "user reach maximum amount of items" in new SetUp {

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

      "user correctly add new item" in new SetUp {
        val item = anItem()
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))

        val correctForm: Seq[(String, String)] = Seq(("country", "PL"), ("reference", "12345"), addActionUrlEncoded())

        val result: Future[Result] =
          controller.saveReferences(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(SEE_OTHER)
      }

      "user save correct data" in new SetUp {
        val item = anItem()
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))

        val correctForm: Seq[(String, String)] =
          Seq(("country", "PL"), ("reference", "12345"), saveAndContinueActionUrlEncoded)

        val result: Future[Result] =
          controller.saveReferences(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.CommodityDetailsController.displayPage(Mode.Normal, item.id)
      }

      "user save correct data without new item" in new SetUp {
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

      "user remove existing item" in new SetUp {

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
