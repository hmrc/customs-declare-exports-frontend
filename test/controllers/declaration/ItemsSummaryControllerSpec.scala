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

package controllers.declaration

import base.CustomExportsBaseSpec
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import models.declaration.governmentagencygoodsitem.{GovernmentAgencyGoodsItem, Packaging}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import play.api.test.Helpers._
import services.cache.{ExportItem, ExportsCacheModel}
import uk.gov.hmrc.auth.core.InsufficientEnrolments

class ItemsSummaryControllerSpec extends CustomExportsBaseSpec with OptionValues {

  private val item1Id = "1234"

  private val item2Id = "5678"

  private val testItem = ExportItem(id = item1Id)

  private val testItem2 = ExportItem(id = item2Id)

  private val cacheModelWith1Item =
    aCacheModel(withItem(testItem), withChoice(SupplementaryDec))

  private val cacheModelWith2Items =
    aCacheModel(withItem(testItem), withItem(testItem2), withChoice(SupplementaryDec))

  private val viewItemsUri = uriWithContextPath("/declaration/export-items")

  private val addItemUri = uriWithContextPath("/declaration/export-items/add")

  private val formId = "PackageInformation"

  override def beforeEach() {
    authorizedUser()
  }

  override def afterEach() {
    reset(mockExportsCacheService)
  }

  private def removeItemUri(id: String) = uriWithContextPath(s"/declaration/export-items/$id/remove")

  "Item Summary Controller" should {

    val supplementaryModel = aCacheModel(withChoice(SupplementaryDec))

    "displayForm" should {

      "return UNAUTHORIZED" when {

        "user does not have EORI" in {
          userWithoutEori()
          withNewCaching(supplementaryModel)

          val result = route(app, getRequest(viewItemsUri, sessionId = supplementaryModel.sessionId)).value
          intercept[InsufficientEnrolments](status(result))

        }
      }

      "return OK" when {

        "user is signed in" in {
          authorizedUser()
          withNewCaching(supplementaryModel)

          val result = route(app, getRequest(viewItemsUri, sessionId = supplementaryModel.sessionId)).value
          val stringResult = contentAsString(result)

          status(result) must be(OK)
          stringResult must include(messages("supplementary.itemsAdd.title"))
          stringResult must include(messages("supplementary.itemsAdd.title.hint"))
        }
      }

      "load data from cache" when {

        "1 export item added before" in {
          authorizedUser()
          withNewCaching(cacheModelWith1Item)

          val result = route(app, getRequest(viewItemsUri, sessionId = cacheModelWith1Item.sessionId)).value
          status(result) must be(OK)

          val stringResult = contentAsString(result)

          stringResult.contains(s"1 Export item added")
        }

        "more than one export item added " in {
          authorizedUser()
          withNewCaching(cacheModelWith2Items)

          val result = route(app, getRequest(viewItemsUri, sessionId = cacheModelWith2Items.sessionId)).value
          status(result) must be(OK)

          val stringResult = contentAsString(result)

          stringResult.contains("1 Export items added")

        }
      }
    }

    "add item" should {
      "add item and redirect" in {

        GovernmentAgencyGoodsItem(sequenceNumeric = 1, packagings = Seq(Packaging()))

        val cachedItem = aCacheModel(withItem(testItem), withChoice(SupplementaryDec))
        withNewCaching(cachedItem)
        when(mockItemGeneratorService.generateItemId()).thenReturn(item1Id)

        val result = route(app, getRequest(addItemUri, sessionId = cachedItem.sessionId)).value
        status(result) must be(SEE_OTHER)

        redirectLocation(result).getOrElse("") must be(routes.ProcedureCodesController.displayPage(item1Id).url)

        val stringResult = contentAsString(result)

        stringResult.contains("1 Export items added")

        verify(mockExportsCacheService).update(any[String], any[ExportsCacheModel])
      }
    }

    "remove item" should {
      "do nothing and redirect back to items" when {
        "cache is empty" in {
          withNewCaching(supplementaryModel)

          val result = route(app, getRequest(removeItemUri("id"), sessionId = supplementaryModel.sessionId)).value
          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(routes.ItemsSummaryController.displayPage().url))
          verifyTheCacheIsUnchanged()
        }

        "item does not exist" in {
          withNewCaching(supplementaryModel)

          val result = route(app, getRequest(removeItemUri("id"), sessionId = supplementaryModel.sessionId)).value
          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(routes.ItemsSummaryController.displayPage().url))
          verifyTheCacheIsUnchanged()
        }
      }

      "update cache and redirect" when {
        "item exists" in {
          val model = aCacheModel(
            withItem(ExportItem("id1", sequenceId = 1)),
            withItem(ExportItem("id2", sequenceId = 2)),
            withChoice(SupplementaryDec)
          )
          withNewCaching(model)

          val result = route(app, getRequest(removeItemUri("id1"), sessionId = model.sessionId)).value
          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(routes.ItemsSummaryController.displayPage().url))
          theCacheModelUpdated.items must be(Set(ExportItem("id2", sequenceId = 1)))
        }
      }
    }

  }
}
