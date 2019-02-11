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

package controllers.supplementary

import base.CustomExportsBaseSpec
import models.{CustomsDeclarationsResponse, NrsSubmissionResponse, SignedInUser}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.mockito.verification.VerificationMode
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.wco.dec.MetaData

import scala.concurrent.Future

class SummaryPageControllerSpec extends CustomExportsBaseSpec {

  private trait test {
    val summaryPageUri = uriWithContextPath("/declaration/supplementary/summary")
    authorizedUser()
  }

  private trait testUsingMocks extends test {
    val onlyOnce: VerificationMode = times(1)

    reset(mockCustomsCacheService)
    reset(mockNrsService)
    reset(mockCustomsDeclarationsConnector)
    reset(mockCustomsDeclareExportsConnector)
    reset(mockMetrics)
  }

  private trait testSubmission extends testUsingMocks {
    val emptyForm: JsValue = JsObject(Map("" -> JsString("")))
    val emptyMetadata: MetaData = MetaData()
    implicit val headerCarrierMock = mock[HeaderCarrier]

    withCaching(None, appConfig.appName)
  }


  "Summary page on displayPage" should {
    "return 200 code" in new test {
      val result = route(app, getRequest(summaryPageUri)).get
      status(result) must be(OK)
    }

    "display \"back\" button that links to Documents Produced page" in new test {
      val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

      resultAsString must include(messages("site.back"))
      resultAsString must include("/declaration/supplementary/add-document")
    }

    "display content for Declaration Type module" in new test {
      val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

      resultAsString must include(messages("supplementary.summary.declarationType.header"))
      resultAsString must include(messages("supplementary.summary.declarationType.dispatchLocation"))
      resultAsString must include(messages("supplementary.summary.declarationType.supplementaryDeclarationType"))
    }

    "display content for Your References module" in new test {
      val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

      resultAsString must include(messages("supplementary.summary.yourReferences.header"))
      resultAsString must include(messages("supplementary.summary.yourReferences.ducr"))
      resultAsString must include(messages("supplementary.summary.yourReferences.lrn"))
    }

    "display content for Parties module" in new test {
      val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

      resultAsString must include(messages("supplementary.summary.parties.header"))
      resultAsString must include(messages("supplementary.summary.parties.exporterId"))
      resultAsString must include(messages("supplementary.summary.parties.declarantId"))
      resultAsString must include(messages("supplementary.summary.parties.representativeId"))
      resultAsString must include(messages("supplementary.summary.parties.representationType"))
      resultAsString must include(messages("supplementary.summary.parties.consigneeId"))
      resultAsString must include(messages("supplementary.summary.parties.additionalParties"))
      resultAsString must include(messages("supplementary.summary.parties.idStatusNumberAuthorisationCode"))
      resultAsString must include(messages("supplementary.summary.parties.authorizedPartyEori"))
    }

    "display content for Locations module" in new test {
      val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

      resultAsString must include(messages("supplementary.summary.locations.header"))
      resultAsString must include(messages("supplementary.summary.locations.dispatchCountry"))
      resultAsString must include(messages("supplementary.summary.locations.destinationCountry"))
      resultAsString must include(messages("supplementary.summary.locations.goodsExaminationCountry"))
      resultAsString must include(messages("supplementary.summary.locations.goodsExaminationLocationType"))
      resultAsString must include(messages("supplementary.summary.locations.qualifierCode"))
      resultAsString must include(messages("supplementary.summary.locations.locationCodeAdditionalQualifier"))
      resultAsString must include(messages("supplementary.summary.locations.procedureCode"))
      resultAsString must include(messages("supplementary.summary.locations.additionalProcedureCodes"))
      resultAsString must include(messages("supplementary.summary.locations.warehouseId"))
      resultAsString must include(messages("supplementary.summary.locations.supervisingCustomsOffice"))
      resultAsString must include(messages("supplementary.summary.locations.officeOfExit"))
    }

    "display content for Transport module" in new test {
      val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

      resultAsString must include(messages("supplementary.summary.transport.header"))
      resultAsString must include(messages("supplementary.summary.transport.inlandTransportMode"))
      resultAsString must include(messages("supplementary.summary.transport.borderTransportMode"))
      resultAsString must include(messages("supplementary.summary.transport.activeMeansOfTransport"))
      resultAsString must include(messages("supplementary.summary.transport.activeMeansOfTransportNationality"))
    }

    "display content for Item module" in new test {
      val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

      resultAsString must include(messages("supplementary.summary.items.header"))
      resultAsString must include(messages("supplementary.summary.items.numberOfItems"))
      resultAsString must include(messages("supplementary.summary.items.amountInvoiced"))
      resultAsString must include(messages("supplementary.summary.items.exchangeRate"))
      resultAsString must include(messages("supplementary.summary.items.transactionType"))
      resultAsString must include(messages("supplementary.summary.items.itemNumber"))
      resultAsString must include(messages("supplementary.summary.items.commodityCode"))
      resultAsString must include(messages("supplementary.summary.items.taricAdditionalCodes"))
      resultAsString must include(messages("supplementary.summary.items.nationalAdditionalCode"))
      resultAsString must include(messages("supplementary.summary.items.tradeDescription"))
      resultAsString must include(messages("supplementary.summary.items.cusCode"))
      resultAsString must include(messages("supplementary.summary.items.statisticalValue"))
      resultAsString must include(messages("supplementary.summary.items.itemPackaging"))
      resultAsString must include(messages("supplementary.summary.items.packageType"))
      resultAsString must include(messages("supplementary.summary.items.supplementaryUnits"))
      resultAsString must include(messages("supplementary.summary.items.shippingMarks"))
      resultAsString must include(messages("supplementary.summary.items.netWeight"))
      resultAsString must include(messages("supplementary.summary.items.grossWeight"))
    }

    "display content for Documents module" in new test {
      val resultAsString = contentAsString(route(app, getRequest(summaryPageUri)).get)

      resultAsString must include(messages("supplementary.summary.previousDocuments.header"))
      resultAsString must include(messages("supplementary.summary.additionalInformation.header"))
      resultAsString must include(messages("supplementary.summary.additionalDocumentation.header"))
    }

    "get the whole supplementary declaration data from cache" in new testUsingMocks {
      withCaching(None, appConfig.appName)
      implicit val hc = mock[HeaderCarrier]

      route(app, getRequest(summaryPageUri)).get.futureValue

      verify(mockCustomsCacheService, onlyOnce).fetch(appConfig.appName)
    }
  }

  "Summary Page on submitSupplementaryDeclaration" should {
    "get the whole supplementary declaration data from cache" in new testSubmission {
      route(app, postRequest(summaryPageUri, emptyForm)).get.futureValue
      verify(mockCustomsCacheService, onlyOnce).fetch(appConfig.appName)
    }

    "send declaration data to Customs Declarations" in new testSubmission {
      route(app, postRequest(summaryPageUri, emptyForm)).get.futureValue
      verify(mockCustomsDeclarationsConnector, onlyOnce).submitExportDeclaration(emptyMetadata)
    }

    "send declaration data to NRS" in new testSubmission {
      route(app, postRequest(summaryPageUri, emptyForm)).get.futureValue

      implicit val signedInUserMock = mock[SignedInUser]
      verify(mockNrsService, onlyOnce).submit(any(), emptyForm.toString, any())
    }

    "save submission response in customs-declare-exports service" in new testSubmission {
      route(app, postRequest(summaryPageUri, emptyForm)).get.futureValue
      verify(mockCustomsDeclareExportsConnector, onlyOnce).saveSubmissionResponse(any())
    }

    "remove supplementary declaration data from cache" in new testSubmission {
      route(app, postRequest(summaryPageUri, emptyForm)).get.futureValue
      verify(mockCustomsCacheService, onlyOnce).remove(appConfig.appName)
    }

    pending
    "return 303 code" in new testSubmission {
      val result = route(app, postRequest(summaryPageUri, emptyForm)).get
      status(result) must be(SEE_OTHER)
    }

    pending
    "redirect to confirmation page" in new testSubmission {
      val result = route(app, postRequest(summaryPageUri, emptyForm)).get.futureValue
      val header = result.header

      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/confirmation")
      )
    }
  }

  "Summary Page on submitSupplementaryDeclaration" when {

    "got error from Customs Declarations" should {
      "display error page" in new testSubmission {
        when(mockCustomsDeclarationsConnector.submitExportDeclaration(emptyMetadata))
          .thenReturn(Future.successful(CustomsDeclarationsResponse(BAD_REQUEST, None)))

        val resultAsString = contentAsString(route(app, postRequest(summaryPageUri, emptyForm)).get)

        resultAsString must include(messages("global.error.title"))
        resultAsString must include(messages("global.error.heading"))
        resultAsString must include(messages("global.error.message"))
      }

      "not remove data from cache" in new testSubmission {
        when(mockCustomsDeclarationsConnector.submitExportDeclaration(emptyMetadata))
          .thenReturn(Future.successful(CustomsDeclarationsResponse(BAD_REQUEST, None)))

        route(app, postRequest(summaryPageUri, emptyForm)).get.futureValue

        verify(mockCustomsCacheService, times(0)).remove(appConfig.appName)
      }
    }

    "got error from NRS" should {
      "continue and save submission response in customs-declare-exports service" in new testSubmission {
        when(mockNrsService.submit(any(), any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(NrsSubmissionResponse("")))

        route(app, postRequest(summaryPageUri, emptyForm)).get.futureValue

        verify(mockCustomsDeclareExportsConnector, onlyOnce).saveSubmissionResponse(any())
      }
    }

    "got exception during saving submission response in customs-declare-exports service" should {
      "display error page" in new testSubmission {
        when(mockCustomsDeclareExportsConnector.saveSubmissionResponse(any())(any(), any()))
          .thenThrow(new RuntimeException())
//          .thenReturn(Future.successful(CustomsDeclareExportsResponse(BAD_REQUEST, "")))

        val resultAsString = contentAsString(route(app, postRequest(summaryPageUri, emptyForm)).get)

        resultAsString must include(messages("global.error.title"))
        resultAsString must include(messages("global.error.heading"))
        resultAsString must include(messages("global.error.message"))
      }

      "remove data from cache" in new testSubmission {
        when(mockCustomsDeclareExportsConnector.saveSubmissionResponse(any())(any(), any()))
          .thenThrow(new RuntimeException())
//          .thenReturn(Future.successful(CustomsDeclareExportsResponse(BAD_REQUEST, "")))

        val resultAsString = contentAsString(route(app, postRequest(summaryPageUri, emptyForm)).get)

        verify(mockCustomsCacheService, onlyOnce).remove(appConfig.appName)
      }
    }

  }




}
