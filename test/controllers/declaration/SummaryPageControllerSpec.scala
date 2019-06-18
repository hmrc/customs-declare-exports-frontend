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
import models.declaration.SupplementaryDeclarationDataSpec
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, times, verify, when}
import org.mockito.verification.VerificationMode
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.wco.dec.MetaData

import scala.concurrent.Future

class SummaryPageControllerSpec extends CustomExportsBaseSpec {

  private trait Test {
    val summaryPageUri = uriWithContextPath("/declaration/summary")
    val emptyForm: JsValue = JsObject(Map("" -> JsString("")))
    val emptyMetadata: MetaData = MetaData(response = Seq.empty)
    val onlyOnce: VerificationMode = times(1)
    val minimumValidCacheData =
      CacheMap(eoriForCache, Map(ConsignmentReferences.id -> correctConsignmentReferencesJSON))

    reset(mockCustomsCacheService)
    reset(mockNrsService)
    reset(mockCustomsDeclareExportsConnector)

    authorizedUser()
    withCaching(None, eoriForCache)
    when(mockCustomsCacheService.fetch(anyString())(any(), any()))
      .thenReturn(Future.successful(Some(minimumValidCacheData)))
    when(mockCustomsCacheService.remove(anyString())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(OK)))
    successfulCustomsDeclareExportsResponse()
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  "Summary Page Controller on display" when {

    "there is data in cache for supplementary declaration" should {
      "return 200 code" in new Test {
        val result = route(app, getRequest(summaryPageUri)).get
        status(result) must be(OK)
      }

      "display 'Back' button that links to 'Export-items' page" in new Test {
        val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("site.back"))
        resultAsString must include("/declaration/transport-details")
      }

      "display 'Accept and submit declaration' button" in new Test {
        val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("site.acceptAndSubmitDeclaration"))
        resultAsString must include("button id=\"submit\" class=\"button\"")
      }

      "display content for Declaration Type module" in new Test {
        val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("supplementary.summary.declarationType.header"))
        resultAsString must include(messages("supplementary.summary.declarationType.dispatchLocation"))
        resultAsString must include(messages("supplementary.summary.declarationType.supplementaryDeclarationType"))
      }

      "display content for Your References module" in new Test {
        val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("supplementary.summary.yourReferences.header"))
        resultAsString must include(messages("supplementary.summary.yourReferences.ducr"))
        resultAsString must include(messages("supplementary.summary.yourReferences.lrn"))
      }

      "display content for Parties module" in new Test {
        when(mockCustomsCacheService.fetch(anyString())(any(), any()))
          .thenReturn(Future.successful(Some(SupplementaryDeclarationDataSpec.cacheMapAllRecords)))
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
      }

      "display content for Locations module" in new Test {
        val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("declaration.summary.locations.header"))
        resultAsString must include(messages("supplementary.summary.locations.dispatchCountry"))
        resultAsString must include(messages("supplementary.summary.locations.destinationCountry"))
        resultAsString must include(messages("supplementary.summary.locations.goodsExaminationAddress"))
        resultAsString must include(messages("supplementary.summary.locations.goodsExaminationLocationType"))
        resultAsString must include(messages("supplementary.summary.locations.qualifierCode"))
        resultAsString must include(messages("supplementary.summary.locations.additionalQualifier"))
        resultAsString must include(messages("supplementary.summary.locations.warehouseId"))
        resultAsString must include(messages("supplementary.summary.locations.supervisingCustomsOffice"))
        resultAsString must include(messages("supplementary.summary.locations.officeOfExit"))
      }

      "display content for Item module" in new Test {
        val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("supplementary.summary.items.header"))
        resultAsString must include(messages("supplementary.summary.items.amountInvoiced"))
        resultAsString must include(messages("supplementary.summary.items.exchangeRate"))
        resultAsString must include(messages("supplementary.summary.items.transactionType"))
      }

      "display containers content with cache available" in new Test {
        when(mockCustomsCacheService.fetch(anyString())(any(), any()))
          .thenReturn(Future.successful(Some(SupplementaryDeclarationDataSpec.cacheMapAllRecords)))

        val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("supplementary.transportInfo.containers.title"))
        resultAsString must include(messages("supplementary.transportInfo.containerId.title"))
        resultAsString must include(messages("M1l3s"))
      }

      "get the whole supplementary declaration data from cache" in new Test {
        route(app, getRequest(summaryPageUri)).get.futureValue
        verify(mockCustomsCacheService, onlyOnce).fetch(any())(any(), any())
      }
    }

    "there is no data in cache for supplementary declaration" should {
      "display error page" in new Test {
        when(mockCustomsCacheService.fetch(anyString())(any(), any()))
          .thenReturn(Future.successful(None))

        val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("supplementary.summary.noData.header"))
        resultAsString must include(messages("supplementary.summary.noData.header.secondary"))
      }
    }

    "there is data in cache, but without LRN" should {
      "display error page" in new Test {
        val cachedData = CacheMap(
          id = eoriForCache,
          data = SupplementaryDeclarationDataSpec.cacheMapAllRecords.data - ConsignmentReferences.id
        )
        when(mockCustomsCacheService.fetch(anyString())(any(), any()))
          .thenReturn(Future.successful(Some(cachedData)))

        val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("supplementary.summary.noData.header"))
        resultAsString must include(messages("supplementary.summary.noData.header.secondary"))
      }
    }

  }

  "Summary Page Controller on submit" when {

    "everything is correct" should {
      "get the whole supplementary declaration data from cache" in new Test {
        route(app, postRequest(summaryPageUri, emptyForm)).get.futureValue
        verify(mockCustomsCacheService, onlyOnce).fetch(any())(any(), any())
      }

      "remove supplementary declaration data from cache" in new Test {
        route(app, postRequest(summaryPageUri, emptyForm)).get.futureValue
        verify(mockCustomsCacheService, onlyOnce).remove(any())(any(), any())
      }

      "return 303 code" in new Test {
        val result = route(app, postRequest(summaryPageUri, emptyForm)).get
        status(result) must be(SEE_OTHER)
      }

      "redirect to confirmation page" in new Test {
        val result = route(app, postRequest(summaryPageUri, emptyForm)).get.futureValue
        val header = result.header

        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/confirmation"))
      }

      "add flash scope with lrn " in new Test {
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
      "display error page" in new Test {
        when(mockCustomsDeclareExportsConnector.submitExportDeclaration(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(BAD_REQUEST)))

        val result = route(app, postRequest(summaryPageUri, emptyForm)).get
        val resultAsString = contentAsString(result)

        resultAsString must include(messages("global.error.title"))
        resultAsString must include(messages("global.error.heading"))
        resultAsString must include(messages("global.error.message"))
      }

      "not remove data from cache" in new Test {
        when(mockCustomsDeclareExportsConnector.submitExportDeclaration(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(BAD_REQUEST)))

        route(app, postRequest(summaryPageUri, emptyForm)).get.futureValue

        verify(mockCustomsCacheService, times(0)).remove(eoriForCache)
      }
    }
  }
}
