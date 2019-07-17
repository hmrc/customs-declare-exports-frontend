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
import forms.Choice
import forms.Choice.choiceId
import org.mockito.Mockito.{reset, verify, when}
import generators.Generators
import org.mockito.ArgumentMatchers
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.test.Helpers._
import services.cache.{ExportItem, ExportsCacheModel}
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import uk.gov.hmrc.wco.dec.{GovernmentAgencyGoodsItem, Packaging}
import org.mockito.ArgumentMatchers.any

import scala.concurrent.Future

class ItemSummaryControllerSpec extends CustomExportsBaseSpec with Generators with PropertyChecks with OptionValues {

  private val viewItemsUri = uriWithContextPath("/declaration/export-items")
  private val addItemUri = uriWithContextPath("/declaration/export-items/add")
  private def removeItemUri(id: String) = uriWithContextPath(s"/declaration/export-items/$id/remove")
  private val formId = "PackageInformation"
  private val item1Id = "1234"
  private val item2Id = "5678"
  private lazy val testItem = ExportItem(id = item1Id)
  private lazy val testItem2 = ExportItem(id = item2Id)
  private lazy val cacheModelWith1Item = createModelWithItems("", items = Set(testItem))
  private lazy val cacheModelWith2Items = createModelWithItems("", items = Set(testItem, testItem2))

  override def beforeEach() {
    authorizedUser()
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  override def afterEach() {
    reset(mockCustomsCacheService, mockExportsCacheService)
  }

  "Item Summary Controller" should {

    "displayForm" should {

      "return UNAUTHORIZED" when {

        "user does not have EORI" in {
          userWithoutEori()
          withNewCaching(createModelWithNoItems())
          withCaching[Seq[GovernmentAgencyGoodsItem]](None)

          val result = route(app, getRequest(viewItemsUri)).value
          intercept[InsufficientEnrolments](status(result))

        }
      }

      "return OK" when {

        "user is signed in" in {
          authorizedUser()
          withNewCaching(createModelWithNoItems())
          withCaching[Seq[GovernmentAgencyGoodsItem]](None)
          withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

          val result = route(app, getRequest(viewItemsUri)).value
          val stringResult = contentAsString(result)

          status(result) must be(OK)
          stringResult must include(messages("supplementary.itemsAdd.title"))
          stringResult must include(messages("supplementary.itemsAdd.title.hint"))
        }
      }

      "load data from cache" when {

        "1 export item added before" in {
          authorizedUser()

          val cachedData = Seq(GovernmentAgencyGoodsItem(sequenceNumeric = 1, packagings = Seq(Packaging())))

          withCaching[Seq[GovernmentAgencyGoodsItem]](Some(cachedData), formId)

          withNewCaching(cacheModelWith1Item)
          val result = route(app, getRequest(viewItemsUri)).value
          status(result) must be(OK)

          val stringResult = contentAsString(result)

          stringResult.contains(s"1 Export item added")
        }

        "more than one export item added " in {
          authorizedUser()

          GovernmentAgencyGoodsItem(sequenceNumeric = 1, packagings = Seq(Packaging()))

          val cachedData = Seq(GovernmentAgencyGoodsItem(sequenceNumeric = 1, packagings = Seq(Packaging())))
          withNewCaching(cacheModelWith2Items)
          when(mockExportsCacheService.get(any[String]))
            .thenReturn(Future.successful(Some(cacheModelWith2Items)))

          withCaching[Seq[GovernmentAgencyGoodsItem]](Some(cachedData), formId)

          val result = route(app, getRequest(viewItemsUri)).value
          status(result) must be(OK)

          val stringResult = contentAsString(result)

          stringResult.contains("1 Export items added")

        }
      }
    }

    "add item" should {
      "add item and redirect" in {

        GovernmentAgencyGoodsItem(sequenceNumeric = 1, packagings = Seq(Packaging()))

        val cachedData = Seq(GovernmentAgencyGoodsItem(sequenceNumeric = 1, packagings = Seq(Packaging())))
        val cachedItem = createModelWithItem("", item = Some(testItem))
        withNewCaching(cachedItem)
        when(mockItemGeneratorService.generateItemId()).thenReturn(item1Id)
        withCaching[Seq[GovernmentAgencyGoodsItem]](Some(cachedData), formId)

        val result = route(app, getRequest(addItemUri)).value
        status(result) must be(SEE_OTHER)

        redirectLocation(result).getOrElse("") must be(routes.ProcedureCodesPageController.displayPage(item1Id).url)

        val stringResult = contentAsString(result)

        stringResult.contains("1 Export items added")

        verify(mockExportsCacheService).update(any[String], any[ExportsCacheModel])
      }

    }

    "remove item" should {
      "do nothing and redirect back to items" when {
        "cache is empty" in {
          withNewCaching()

          val result = route(app, getRequest(removeItemUri("id"))).value
          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(routes.ItemsSummaryController.displayPage().url))
          verifyTheCacheIsUnchanged()
        }

        "item does not exist" in {
          withNewCaching(createModelWithNoItems())

          val result = route(app, getRequest(removeItemUri("id"))).value
          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(routes.ItemsSummaryController.displayPage().url))
          verifyTheCacheIsUnchanged()
        }
      }

      "update cache and redirect" when {
        "item exists" in {
          withNewCaching(createModelWithItems("", Set(ExportItem("id1"), ExportItem("id2"))))

          val result = route(app, getRequest(removeItemUri("id1"))).value
          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(routes.ItemsSummaryController.displayPage().url))
          theCacheModelUpdated.items must be(Set(ExportItem("id2")))
        }
      }
    }

  }
}
