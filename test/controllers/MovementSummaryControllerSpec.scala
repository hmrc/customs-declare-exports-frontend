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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{atLeastOnce, verify, when}
import org.scalatest.BeforeAndAfter
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers._
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest
import base.ExportsTestData._
import scala.concurrent.Future

class MovementSummaryControllerSpec
  extends CustomExportsBaseSpec
    with BeforeAndAfter {

  private val uri = uriWithContextPath("/movement/summary-page")
  private val emptyForm = JsObject(Map("" -> JsString("")))

  before {
    authorizedUser()
  }

  "MovementSummaryController.displaySummary()" when {

    "cannot read data from DB" should {

      "return 500 code" in {
        mockCacheServiceFetchAndGetEntryResultWith(None)

        val result = route(app, getRequest(uri)).get

        status(result) must be(INTERNAL_SERVER_ERROR)
      }

      "display error page for DB problem" in {
        mockCacheServiceFetchAndGetEntryResultWith(None)

        val result = route(app, getRequest(uri)).get

        contentAsString(result) must include(
          messagesApi("global.error.heading"))
      }
    }

    "can read data from DB" should {

      "return 200 code" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EAL")))

        val result = route(app, getRequest(uri)).get

        status(result) must be(OK)
      }

      "display summary page with warning" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EAL")))

        val result = route(app, getRequest(uri)).get
        val stringResult = contentAsString(result)

        val warningIconTag = "<i class=\"icon icon-important\">"
        stringResult must include(warningIconTag)
        stringResult must include(
          messages("movement.summaryPage.warningMessage"))
      }

      "display summary page with the data table" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EAL")))

        val result = route(app, getRequest(uri)).get
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

        val result = route(app, getRequest(uri)).get

        contentAsString(result) must include(
          messages("movement.summaryPage.confirmationNotice"))
      }
    }
  }

  "MovementSummaryController.submitMovementRequest" when {

    "cannot read data from DB" should {

      "return 500 code" in {
        mockCacheServiceFetchAndGetEntryResultWith(None)

        val result = route(app, postRequest(uri, emptyForm)).get

        status(result) must be(INTERNAL_SERVER_ERROR)
      }

      "display error page for DB problem" in {
        mockCacheServiceFetchAndGetEntryResultWith(None)

        val result = route(app, postRequest(uri, emptyForm)).get

        contentAsString(result) must include(
          messagesApi("global.error.heading"))
      }
    }

    "can read data from DB but submission failed" should {

      "return 500 code" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EAL")))
        sendMovementRequest400Response()

        val result = route(app, postRequest(uri, emptyForm)).get

        status(result) must be(INTERNAL_SERVER_ERROR)
      }

      "display error page" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EAL")))
        sendMovementRequest400Response()

        val result = route(app, postRequest(uri, emptyForm)).get

        contentAsString(result) must include(
          messagesApi("global.error.heading"))
      }

      "clean the cache" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EAL")))
        sendMovementRequest400Response()

        route(app, postRequest(uri, emptyForm)).get

        verify(mockCustomsCacheService, atLeastOnce).remove(any())(any(), any())
      }
    }

    "can read data from DB and submission succeeded" should {

      "return 200 code" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EAL")))
        sendMovementRequest()

        val result = route(app, postRequest(uri, emptyForm)).get

        status(result) must be(OK)
      }

      "display confirmation page for Arrival" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EAL")))
        sendMovementRequest()

        val result = route(app, postRequest(uri, emptyForm)).get

        contentAsString(result) must include(
          messagesApi("movement.choice.EAL") + " has been submitted")
      }

      "display confirmation page for Departure" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EDL")))
        sendMovementRequest()

        val result = route(app, postRequest(uri, emptyForm)).get
        result.value

        contentAsString(result) must include(
          messagesApi("movement.choice.EDL") + " has been submitted")
      }

      "clean the cache" in {
        mockCacheServiceFetchAndGetEntryResultWith(
          Some(validMovementRequest("EAL")))
        sendMovementRequest()

        route(app, postRequest(uri, emptyForm)).get

        verify(mockCustomsCacheService, atLeastOnce).remove(any())(any(), any())
      }
    }
  }

  private def mockCacheServiceFetchAndGetEntryResultWith(
    desiredResult: Option[InventoryLinkingMovementRequest]) =
    when(
      mockCustomsCacheService.fetchMovementRequest(any(), any())(any(), any()))
      .thenReturn(Future.successful(desiredResult))

}
