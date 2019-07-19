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
import forms.declaration.ConsignmentReferencesSpec.correctConsignmentReferencesJSON
import forms.declaration.{ConsignmentReferences, ConsignmentReferencesSpec}
import models.declaration.SupplementaryDeclarationTestData
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito._
import org.mockito.verification.VerificationMode
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class SummaryPageControllerSpec extends CustomExportsBaseSpec {

  val summaryPageUri = uriWithContextPath("/declaration/summary")
  val emptyForm: JsValue = JsObject(Map("" -> JsString("")))
  val onlyOnce: VerificationMode = times(1)
  val minimumValidCacheData =
    CacheMap(eoriForCache, Map(ConsignmentReferences.id -> correctConsignmentReferencesJSON))

  override def beforeEach() {
    super.beforeEach()
    authorizedUser()
    successfulCustomsDeclareExportsResponse()
    withNewCaching(SupplementaryDeclarationTestData.allRecords)

    //TODO: Below, these three mocks will have to be deleted as part of the mapping story
    when(mockCustomsCacheService.fetch(anyString())(any(), any()))
      .thenReturn(Future.successful(Some(minimumValidCacheData)))
    when(mockCustomsCacheService.remove(anyString())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(OK)))
  }

  override def afterEach() {
    super.afterEach()
    reset(mockCustomsCacheService, mockExportsCacheService, mockNrsService, mockCustomsDeclareExportsConnector)
  }

  "Summary Page Controller on display" when {

    "there is data in cache for supplementary declaration" should {
      "return 200 code" in {
        val result = route(app, getRequest(summaryPageUri)).get
        status(result) must be(OK)
        verify(mockExportsCacheService, times(2)).get(anyString)
      }

      "display 'Back' button that links to 'Export-items' page" in {

        withNewCaching(SupplementaryDeclarationTestData.allRecords.copy(containerData = None))
        val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("site.back"))
        resultAsString must include("/declaration/transport-details")
        verify(mockExportsCacheService, times(2)).get(anyString)
      }

      "display 'Accept and submit declaration' button" in {
        val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("site.acceptAndSubmitDeclaration"))
        resultAsString must include("button id=\"submit\" class=\"button\"")
        verify(mockExportsCacheService, times(2)).get(anyString)
      }

      "display content for Declaration Type module" in {
        val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("supplementary.summary.declarationType.header"))
        resultAsString must include(messages("supplementary.summary.declarationType.dispatchLocation"))
        resultAsString must include(messages("supplementary.summary.declarationType.supplementaryDeclarationType"))
        verify(mockExportsCacheService, times(2)).get(anyString)
      }

      "display content for Your References module" in {
        val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("supplementary.summary.yourReferences.header"))
        resultAsString must include(messages("supplementary.summary.yourReferences.ducr"))
        resultAsString must include(messages("supplementary.summary.yourReferences.lrn"))
        verify(mockExportsCacheService, times(2)).get(anyString)
      }

      "display content for Parties module" in {
        when(mockCustomsCacheService.fetch(anyString())(any(), any()))
          .thenReturn(Future.successful(Some(SupplementaryDeclarationTestData.cacheMapAllRecords)))
        val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("supplementary.summary.parties.header"))
        resultAsString must include(messages("supplementary.summary.parties.exporterId"))
        resultAsString must include(messages("supplementary.summary.parties.exporterAddress"))
        resultAsString must include(messages("supplementary.summary.parties.declarantId"))
        resultAsString must include(messages("supplementary.summary.parties.declarantAddress"))
        resultAsString must include(messages("supplementary.summary.parties.representativeId"))
        resultAsString must include(messages("supplementary.summary.parties.representativeAddress"))
        resultAsString must include(messages("supplementary.summary.parties.representationType"))
        resultAsString must include(messages("supplementary.summary.parties.additionalParties.id"))
        resultAsString must include(messages("supplementary.summary.parties.additionalParties.type"))
        resultAsString must include(messages("supplementary.summary.parties.idStatusNumberAuthorisationCode"))
        resultAsString must include(messages("supplementary.summary.parties.authorizedPartyEori"))
        verify(mockExportsCacheService, times(2)).get(anyString)
      }

      "display content for Locations module" in {
        val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("declaration.summary.locations.header"))
        resultAsString must include(messages("supplementary.summary.locations.dispatchCountry"))
        resultAsString must include(messages("supplementary.summary.locations.destinationCountry"))
        resultAsString must include(messages("supplementary.summary.locations.goodsExaminationAddress"))
        resultAsString must include(messages("supplementary.summary.locations.goodsExaminationLocationType"))
        resultAsString must include(messages("supplementary.summary.locations.qualifierCode"))
        resultAsString must include(messages("supplementary.summary.locations.additionalQualifier"))
        resultAsString must include(messages("supplementary.summary.locations.warehouseType"))
        resultAsString must include(messages("supplementary.summary.locations.warehouseId"))
        resultAsString must include(messages("supplementary.summary.locations.supervisingCustomsOffice"))
        resultAsString must include(messages("supplementary.summary.locations.officeOfExit"))
        verify(mockExportsCacheService, times(2)).get(anyString)
      }

      "display content for Item module" in {
        val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("supplementary.summary.items.header"))
        resultAsString must include(messages("supplementary.summary.items.amountInvoiced"))
        resultAsString must include(messages("supplementary.summary.items.exchangeRate"))
        resultAsString must include(messages("supplementary.summary.items.transactionType"))
        verify(mockExportsCacheService, times(2)).get(anyString)
      }

      "display containers content with cache available" in {
        val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("supplementary.transportInfo.containers.title"))
        resultAsString must include(messages("supplementary.transportInfo.containerId.title"))
        resultAsString must include(messages("M1l3s"))
        verify(mockExportsCacheService, times(2)).get(anyString)
      }

      "get the whole supplementary declaration data from cache" in {
        route(app, getRequest(summaryPageUri)).get.futureValue
        verify(mockCustomsCacheService, never()).fetch(any())(any(), any())
        verify(mockExportsCacheService, times(2)).get(anyString)
      }
    }

    "there is no data in cache for supplementary declaration" should {
      "display error page" in {
        withNewCaching(createModelWithNoItems("SMP"))
        val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("supplementary.summary.noData.header"))
        resultAsString must include(messages("supplementary.summary.noData.header.secondary"))
        verify(mockExportsCacheService, times(2)).get(anyString)
      }
    }

    "there is data in cache, but without LRN" should {
      "display error page" in {
        withNewCaching(SupplementaryDeclarationTestData.allRecords.copy(consignmentReferences = None))

        val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("supplementary.summary.noData.header"))
        resultAsString must include(messages("supplementary.summary.noData.header.secondary"))
        verify(mockExportsCacheService, times(2)).get(anyString)
      }
    }

  }

  "Summary Page Controller on submit" when {

    "everything is correct" should {
      "get the whole supplementary declaration data from cache" in {
        route(app, postRequest(summaryPageUri, emptyForm)).get.futureValue
        verify(mockCustomsCacheService, onlyOnce).fetch(any())(any(), any())
      }

      "remove supplementary declaration data from cache" in {
        route(app, postRequest(summaryPageUri, emptyForm)).get.futureValue
        verify(mockCustomsCacheService, onlyOnce).remove(any())(any(), any())
      }

      "return 303 code" in {
        val result = route(app, postRequest(summaryPageUri, emptyForm)).get
        status(result) must be(SEE_OTHER)
      }

      "redirect to confirmation page" in {
        val result = route(app, postRequest(summaryPageUri, emptyForm)).get.futureValue
        val header = result.header

        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/confirmation"))
      }

      "add flash scope with lrn " in {
        val cacheData = Map(ConsignmentReferences.id -> ConsignmentReferencesSpec.correctConsignmentReferencesJSON)
        when(mockCustomsCacheService.fetch(anyString())(any(), any()))
          .thenReturn(Future.successful(Some(CacheMap(eoriForCache, cacheData))))

        val result = route(app, postRequest(summaryPageUri, emptyForm)).get

        val f = flash(result)
        f.get("LRN") must be(defined)
        f("LRN") must equal("123LRN")
      }
    }

    "got error from Customs Declarations" should {
      "display error page" in {
        when(mockCustomsDeclareExportsConnector.submitExportDeclaration(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(BAD_REQUEST)))

        val result = route(app, postRequest(summaryPageUri, emptyForm)).get
        val resultAsString = contentAsString(result)

        resultAsString must include(messages("global.error.title"))
        resultAsString must include(messages("global.error.heading"))
        resultAsString must include(messages("global.error.message"))
      }

      "not remove data from cache" in {
        when(mockCustomsDeclareExportsConnector.submitExportDeclaration(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(BAD_REQUEST)))

        route(app, postRequest(summaryPageUri, emptyForm)).get.futureValue

        verify(mockCustomsCacheService, never()).remove(eoriForCache)
      }
    }
  }
}
