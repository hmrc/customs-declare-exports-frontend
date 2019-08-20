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

import base.{CustomExportsBaseSpec, TestHelper}
import forms.Choice.AllowedChoiceValues
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import models.declaration.SupplementaryDeclarationTestData
import models.requests.{ExportsSessionKeys, JourneyRequest}
import models.{DeclarationStatus, ExportsDeclaration}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito._
import org.mockito.verification.VerificationMode
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class SummaryControllerSpec extends CustomExportsBaseSpec {

  import SupplementaryDeclarationTestData._

  val summaryPageUri = uriWithContextPath("/declaration/summary")
  val emptyForm: JsValue = JsObject(Map("" -> JsString("")))
  val onlyOnce: VerificationMode = times(1)
  val onlyTwice: VerificationMode = times(2)

  implicit val request = TestHelper.journeyRequest(FakeRequest("", ""), AllowedChoiceValues.SupplementaryDec)

  override def beforeEach() {
    super.beforeEach()
    authorizedUser()
    successfulCustomsDeclareExportsResponse()
    withNewCaching(allRecords)
  }

  override def afterEach() {
    super.afterEach()
    reset(mockExportsCacheService, mockNrsService, mockCustomsDeclareExportsConnector, mockSubmissionService)
  }

  "Summary Page Controller on display" when {

    "there is data in cache for supplementary declaration" should {
      "return 200 code" in {
        val result = route(app, getRequest(summaryPageUri)).get
        status(result) must be(OK)
        verify(mockExportsCacheService).get(anyString)(any())
      }

      "display 'Back' button that links to 'Export-items' page" in {
        val model = SupplementaryDeclarationTestData.allRecords.copy(containerData = None)
        withNewCaching(model)

        val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("site.back"))
        resultAsString must include("/declaration/transport-details")
        verify(mockExportsCacheService).get(anyString)(any())
      }

      "display 'Accept and submit declaration' button" in {
        val resultAsString =
          contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("site.acceptAndSubmitDeclaration"))
        resultAsString must include("button id=\"submit\" class=\"button\"")
        verify(mockExportsCacheService).get(anyString)(any())
      }

      "display content for Declaration Type module" in {
        val resultAsString =
          contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("supplementary.summary.declarationType.header"))
        resultAsString must include(messages("supplementary.summary.declarationType.dispatchLocation"))
        resultAsString must include(messages("supplementary.summary.declarationType.supplementaryDeclarationType"))
        verify(mockExportsCacheService).get(anyString)(any())
      }

      "display content for Your References module" in {
        val resultAsString =
          contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("supplementary.summary.yourReferences.header"))
        resultAsString must include(messages("supplementary.summary.yourReferences.ducr"))
        resultAsString must include(messages("supplementary.summary.yourReferences.lrn"))
        verify(mockExportsCacheService).get(anyString)(any())
      }

      "display content for Parties module" in {
        val resultAsString =
          contentAsString(route(app, getRequest(summaryPageUri)).get)

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
        verify(mockExportsCacheService).get(anyString)(any())
      }

      "display content for Locations module" in {
        val resultAsString =
          contentAsString(route(app, getRequest(summaryPageUri)).get)

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
        verify(mockExportsCacheService).get(anyString)(any())
      }

      "display content for Item module" in {
        val resultAsString =
          contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("supplementary.summary.items.header"))
        resultAsString must include(messages("supplementary.summary.items.amountInvoiced"))
        resultAsString must include(messages("supplementary.summary.items.exchangeRate"))
        resultAsString must include(messages("supplementary.summary.items.transactionType"))
        verify(mockExportsCacheService).get(anyString)(any())
      }

      "display containers content with cache available" in {
        val resultAsString =
          contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("supplementary.transportInfo.containers.title"))
        resultAsString must include(messages("supplementary.transportInfo.containerId.title"))
        resultAsString must include(messages("M1l3s"))
        verify(mockExportsCacheService).get(anyString)(any())
      }

      "get the whole supplementary declaration data from cache" in {
        route(app, getRequest(summaryPageUri)).get.futureValue
        verify(mockExportsCacheService).get(anyString)(any())
      }
    }

    "there is no data in cache for supplementary declaration" should {
      "display error page" in {
        val model = aDeclaration(withChoice(SupplementaryDec))
        withNewCaching(model)
        val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("supplementary.summary.noData.header"))
        resultAsString must include(messages("supplementary.summary.noData.header.secondary"))
        verify(mockExportsCacheService).get(anyString)(any())
      }
    }

    "there is data in cache, but without LRN" should {
      "display error page" in {
        val model = SupplementaryDeclarationTestData.allRecords.copy(consignmentReferences = None)
        withNewCaching(model)

        val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

        resultAsString must include(messages("supplementary.summary.noData.header"))
        resultAsString must include(messages("supplementary.summary.noData.header.secondary"))
        verify(mockExportsCacheService).get(anyString)(any())
      }
    }

  }

  "Summary Page Controller on submit" when {
    "everything is correct" should {
      "submit successfully" in {
        when(
          mockSubmissionService.submit(any[ExportsDeclaration])(
            any[JourneyRequest[_]],
            any[HeaderCarrier],
            any[ExecutionContext]
          )
        ).thenReturn(Future.successful(Some("123LRN")))

        val result = route(app, postRequest(summaryPageUri, emptyForm)).get

        status(result) must be(SEE_OTHER)
        session(result).get(ExportsSessionKeys.declarationId) must be(None)
        redirectLocation(result) must be(Some(controllers.declaration.routes.ConfirmationController.displayPage().url))
        flash(result).get("LRN") must be(Some("123LRN"))
        verify(mockSubmissionService).submit(any[ExportsDeclaration])(any[JourneyRequest[_]], any[HeaderCarrier], any[ExecutionContext])
      }
    }

    "got error from Customs Declarations" should {
      "display error page" in {
        when(
          mockSubmissionService.submit(any[ExportsDeclaration])(
            any[JourneyRequest[_]],
            any[HeaderCarrier],
            any[ExecutionContext]
          )
        ).thenReturn(Future.successful(None))

        val result = route(app, postRequest(summaryPageUri, emptyForm)).get
        val resultAsString = contentAsString(result)

        resultAsString must include(messages("global.error.title"))
        resultAsString must include(messages("global.error.heading"))
        resultAsString must include(messages("global.error.message"))
      }
    }
  }
}
