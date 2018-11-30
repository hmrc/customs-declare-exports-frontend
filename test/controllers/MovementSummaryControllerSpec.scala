/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers

import base.CustomExportsBaseSpec
import base.ExportsTestData._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfter
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest

import scala.concurrent.Future

class MovementSummaryControllerSpec
    extends CustomExportsBaseSpec
    with BeforeAndAfter {

  private val uriSummary = uriWithContextPath("/movement/summary")
  private val uriConfirmation = uriWithContextPath("/movement/confirmation")

  private val emptyForm = JsObject(Map("" -> JsString("")))

  before {
    authorizedUser()
    reset(mockCustomsCacheService)
    reset(mockCustomsInventoryLinkingExportsConnector)
  }

  "MovementSummaryController.displaySummary()" when {

    "cannot read data from DB" should {
      "return 500 code" in {
        mockCacheServiceFetchAndGetEntryResultWith(None)

        val result = route(app, getRequest(uriSummary)).get

        status(result) must be(INTERNAL_SERVER_ERROR)
      }

      "display error page for DB problem" in {
        mockCacheServiceFetchAndGetEntryResultWith(None)

        val result = route(app, getRequest(uriSummary)).get

        contentAsString(result) must include(
          messagesApi("global.error.heading"))
      }
    }

    "can read data from DB" should {
      "return 200 code" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EAL")))

        val result = route(app, getRequest(uriSummary)).get

        status(result) must be(OK)
      }

      "display summary page with warning" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EAL")))

        val result = route(app, getRequest(uriSummary)).get
        val stringResult = contentAsString(result)

        val warningIconTag = "<i class=\"icon icon-important\">"
        stringResult must include(warningIconTag)
        stringResult must include(
          messages("movement.summaryPage.warningMessage"))
      }

      "display summary page with the data table" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EAL")))

        val result = route(app, getRequest(uriSummary)).get
        val stringResult = contentAsString(result)

        stringResult must include("table")
        stringResult must include("tbody")
        stringResult must include("td class=\"previous-question-title bold\"")
        stringResult must include("td class=\"previous-question-body\"")

        stringResult must include(messages("movement.eori"))
        stringResult must include(messages("movement.ucr"))
        stringResult must include(messages("movement.ucrType"))
        stringResult must include(messages("movement.goodsLocation"))
      }

      "display summary page with submission confirmation notice" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EAL")))

        val result = route(app, getRequest(uriSummary)).get

        contentAsString(result) must include(
          messages("movement.summaryPage.confirmationNotice"))
      }
    }
  }

  "MovementSummaryController.submitMovementRequest" when {

    "cannot read data from DB" should {
      "return 500 code" in {
        mockCacheServiceFetchAndGetEntryResultWith(None)

        val result = route(app, postRequest(uriSummary, emptyForm)).get

        status(result) must be(INTERNAL_SERVER_ERROR)
      }

      "display error page for DB problem" in {
        mockCacheServiceFetchAndGetEntryResultWith(None)

        val result = route(app, postRequest(uriSummary, emptyForm)).get

        contentAsString(result) must include(
          messagesApi("global.error.heading"))
      }
    }

    "can read data from DB but submission failed" should {

      "return 500 code" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EAL")))
        sendMovementRequest400Response()

        val result = route(app, postRequest(uriSummary, emptyForm)).get

        status(result) must be(INTERNAL_SERVER_ERROR)
      }

      "display error page" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EAL")))
        sendMovementRequest400Response()

        val result = route(app, postRequest(uriSummary, emptyForm)).get

        contentAsString(result) must include(
          messagesApi("global.error.heading"))
      }
    }

    "can read data from DB and submission succeeded" should {
      "fetch data from CustomsCacheService" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EAL")))
        sendMovementRequest()

        route(app, postRequest(uriSummary, emptyForm)).get.futureValue

        verify(mockCustomsCacheService)
          .fetchMovementRequest(any(), any())(any(), any())
      }

      "call CustomsInventoryLinkingExportsConnector" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EAL")))
        sendMovementRequest()

        route(app, postRequest(uriSummary, emptyForm)).get.futureValue

        verify(mockCustomsInventoryLinkingExportsConnector)
          .sendMovementRequest(any(), any())(any(), any())
      }

      "redirect to confirmation page" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EAL")))
        sendMovementRequest()

        val result = route(app, postRequest(uriSummary, emptyForm)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(
          Some("/customs-declare-exports/movement/confirmation"))
      }
    }

    "MovementSummaryController.displayConfirmation" should {
      "fetch data from CustomsCacheService" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EAL")))
        mockCacheCleared()

        route(app, getRequest(uriConfirmation)).get.futureValue

        verify(mockCustomsCacheService)
          .fetchMovementRequest(any(), any())(any(), any())
      }

      "clean the cache" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EAL")))
        mockCacheCleared()

        route(app, getRequest(uriConfirmation)).get.futureValue

        verify(mockCustomsCacheService).remove(any())(any(), any())
      }

      "display confirmation page for Arrival" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EAL")))
        mockCacheCleared()

        val result = route(app, getRequest(uriConfirmation)).get

        contentAsString(result) must include(
          messagesApi("movement.choice.EAL") + " has been submitted")
      }

      "display confirmation page for Departure" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EDL")))
        mockCacheCleared()

        val result = route(app, getRequest(uriConfirmation)).get

        contentAsString(result) must include(
          messagesApi("movement.choice.EDL") + " has been submitted")
      }
    }

  }

  private def mockCacheServiceFetchAndGetEntryResultWith(
      desiredResult: Option[InventoryLinkingMovementRequest]) =
    when(
      mockCustomsCacheService.fetchMovementRequest(any(), any())(any(), any()))
      .thenReturn(Future.successful(desiredResult))

  private def mockCacheCleared() =
    when(mockCustomsCacheService.remove(any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(ACCEPTED)))

}
