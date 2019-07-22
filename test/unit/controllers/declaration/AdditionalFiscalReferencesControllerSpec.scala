/*
 * Copyright 2019 HM Revenue & Customs
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

import controllers.declaration.AdditionalFiscalReferencesController
import controllers.util.Remove
import forms.Choice
import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.Helpers._
import services.cache.{ExportItem, ExportsCacheModel}
import unit.base.ControllerSpec
import views.html.declaration.additional_fiscal_references

import scala.concurrent.Future

class AdditionalFiscalReferencesControllerSpec extends ControllerSpec {

  trait SetUp {

    val additionalFiscalReferencesPage = new additional_fiscal_references(mainTemplate)

    val controller = new AdditionalFiscalReferencesController(
      mockAuthAction,
      mockJourneyAction,
      mockErrorHandler,
      mockCustomsCacheService,
      mockExportsCacheService,
      stubMessagesControllerComponents(),
      additionalFiscalReferencesPage
    )(minimalAppConfig, ec)

    authorizedUser()
    withCaching(None)
    withNewCaching(createModelWithNoItems(Choice.AllowedChoiceValues.SupplementaryDec))
  }

  "Additional fiscal references controller" should {

    "return 200 (OK)" when {

      "display page method is invoked with empty cache" in new SetUp {
        val result = controller.displayPage("itemId")(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked with empty additional fiscal references" in new SetUp {
        val itemCacheData = ExportItem("itemId", additionalFiscalReferencesData = None)
        val cachedData: ExportsCacheModel =
          createModelWithNoItems(Choice.AllowedChoiceValues.SupplementaryDec).copy(items = Set(itemCacheData))
        withNewCaching(cachedData)

        val result = controller.displayPage("itemId")(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked with data in cache" in new SetUp {

        val itemCacheData = ExportItem(
          "itemId",
          additionalFiscalReferencesData =
            Some(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("PL", "12345"))))
        )
        val cachedData =
          createModelWithNoItems(Choice.AllowedChoiceValues.SupplementaryDec).copy(items = Set(itemCacheData))
        withNewCaching(cachedData)

        val result = controller.displayPage("itemId")(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user provide wrong action" in new SetUp {

        val wrongAction = Seq(("country", "PL"), ("reference", "12345"), ("WrongAction", ""))

        val result = controller.saveReferences("itemId")(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 400 (BAD_REQUEST) during adding" when {

      "user put incorrect data" in new SetUp {

        val incorrectForm = Seq(("country", "PL"), ("reference", "!@#$"), addActionUrlEncoded)

        val result = controller.saveReferences("itemId")(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user put duplicated item" in new SetUp {

        val itemCacheData = ExportItem(
          "itemId",
          additionalFiscalReferencesData =
            Some(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("PL", "12345"))))
        )
        val cachedData =
          createModelWithNoItems(Choice.AllowedChoiceValues.SupplementaryDec).copy(items = Set(itemCacheData))
        withNewCaching(cachedData)

        val duplicatedForm = Seq(("country", "PL"), ("reference", "12345"), addActionUrlEncoded)

        val result = controller.saveReferences("itemId")(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reach maximum amount of items" in new SetUp {

        val itemCacheData = ExportItem(
          "itemId",
          additionalFiscalReferencesData =
            Some(AdditionalFiscalReferencesData(Seq.fill(99)(AdditionalFiscalReference("PL", "12345"))))
        )
        val cachedData =
          createModelWithNoItems(Choice.AllowedChoiceValues.SupplementaryDec).copy(items = Set(itemCacheData))
        withNewCaching(cachedData)

        val form = Seq(("country", "PL"), ("reference", "54321"), addActionUrlEncoded)

        val result = controller.saveReferences("itemId")(postRequestAsFormUrlEncoded(form: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 400 (BAD_REQUEST) during saving" when {

      "user put incorrect data" in new SetUp {

        val incorrectForm = Seq(("country", "PL"), ("reference", "!@#$"), saveAndContinueActionUrlEncoded)

        val result = controller.saveReferences("itemId")(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user put duplicated item" in new SetUp {

        val itemCacheData = ExportItem(
          "itemId",
          additionalFiscalReferencesData =
            Some(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("PL", "12345"))))
        )
        val cachedData =
          createModelWithNoItems(Choice.AllowedChoiceValues.SupplementaryDec).copy(items = Set(itemCacheData))
        withNewCaching(cachedData)

        val duplicatedForm = Seq(("country", "PL"), ("reference", "12345"), saveAndContinueActionUrlEncoded)

        val result = controller.saveReferences("itemId")(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reach maximum amount of items" in new SetUp {

        val itemCacheData = ExportItem(
          "itemId",
          additionalFiscalReferencesData =
            Some(AdditionalFiscalReferencesData(Seq.fill(99)(AdditionalFiscalReference("PL", "12345"))))
        )
        val cachedData =
          createModelWithNoItems(Choice.AllowedChoiceValues.SupplementaryDec).copy(items = Set(itemCacheData))
        withNewCaching(cachedData)

        val form = Seq(("country", "PL"), ("reference", "54321"), saveAndContinueActionUrlEncoded)

        val result = controller.saveReferences("itemId")(postRequestAsFormUrlEncoded(form: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user correctly add new item" in new SetUp {

        val correctForm = Seq(("country", "PL"), ("reference", "12345"), addActionUrlEncoded)

        val result = controller.saveReferences("itemId")(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(SEE_OTHER)
      }

      "user save correct data" in new SetUp {

        val correctForm = Seq(("country", "PL"), ("reference", "12345"), saveAndContinueActionUrlEncoded)

        val result = controller.saveReferences("itemId")(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/items/itemId/item-type"))
      }

      "user save correct data without new item" in new SetUp {
        val itemCacheData = ExportItem(
          "itemId",
          additionalFiscalReferencesData =
            Some(AdditionalFiscalReferencesData(Seq.fill(99)(AdditionalFiscalReference("PL", "12345"))))
        )
        val cachedData =
          createModelWithNoItems(Choice.AllowedChoiceValues.SupplementaryDec).copy(items = Set(itemCacheData))
        withNewCaching(cachedData)

        val correctForm = saveAndContinueActionUrlEncoded

        val result = controller.saveReferences("itemId")(postRequestAsFormUrlEncoded(correctForm))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/items/itemId/item-type"))
      }

      "user remove existing item" in new SetUp {

        val itemCacheData = ExportItem(
          "itemId",
          additionalFiscalReferencesData =
            Some(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("PL", "12345"))))
        )
        val cachedData =
          createModelWithNoItems(Choice.AllowedChoiceValues.SupplementaryDec).copy(items = Set(itemCacheData))
        withNewCaching(cachedData)

        val removeForm = (Remove.toString, "0")

        val result = controller.saveReferences("itemId")(postRequestAsFormUrlEncoded(removeForm))

        status(result) must be(SEE_OTHER)
      }
    }
  }
}
