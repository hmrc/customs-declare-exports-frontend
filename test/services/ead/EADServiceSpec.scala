/*
 * Copyright 2022 HM Revenue & Customs
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

package services.ead

import base.{Injector, MockConnectors, MockExportCacheService, UnitWithMocksSpec}
import com.dmanchester.playfop.sapi.PlayFop
import connectors.CustomsDeclareExportsConnector
import models.dis.MrnStatusSpec
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import play.api.i18n.Messages
import play.api.test.Helpers
import play.api.test.Helpers._
import services.cache.SubmissionBuilder
import uk.gov.hmrc.http.HeaderCarrier
import views.xml.pdf.pdfTemplate

import scala.concurrent.{ExecutionContext, Future}
import views.helpers.ViewDates

class EADServiceSpec
    extends UnitWithMocksSpec with MockExportCacheService with MockConnectors with ScalaFutures with OptionValues with Injector
    with SubmissionBuilder {

  private val connector = mock[CustomsDeclareExportsConnector]
  private val barcodeService = instanceOf[BarcodeService]
  private val pdfTemplate = instanceOf[pdfTemplate]
  private val playFop = instanceOf[PlayFop]
  private implicit val hc: HeaderCarrier = mock[HeaderCarrier]
  private implicit val ec: ExecutionContext = ExecutionContext.global
  private implicit val messages: Messages = Helpers.stubMessages()
  private val service = new EADService(barcodeService, pdfTemplate, playFop, connector)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(connector)
  }

  "EADService" should {

    "should return a PDF as an Array of bytes" when {
      val mrn = "18GBAKZ81EQJ2FGVR"

      "mrn is valid" in {
        // Given
        when(connector.fetchMrnStatus(any[String])(any(), any())).thenReturn(Future.successful(Some(MrnStatusSpec.completeMrnStatus)))

        // When
        val rawPdf: Array[Byte] = await(service.generatePdf(mrn))

        // Then
        val pdfDocument = PDDocument.load(rawPdf)

        try {
          val pdfData = new PDFTextStripper().getText(pdfDocument)
          pdfData must include(mrn)
          pdfData must include(MrnStatusSpec.completeMrnStatus.eori)
          pdfData must include(MrnStatusSpec.completeMrnStatus.declarationType)
          pdfData must include(MrnStatusSpec.completeMrnStatus.ucr.get)
          pdfData must include(MrnStatusSpec.completeMrnStatus.totalPackageQuantity)
          pdfData must include(MrnStatusSpec.completeMrnStatus.goodsItemQuantity)
          pdfData must include(ViewDates.formatDateAtTime(MrnStatusSpec.completeMrnStatus.releasedDateTime.get))
          pdfData must include(ViewDates.formatDateAtTime(MrnStatusSpec.completeMrnStatus.acceptanceDateTime.get))
          pdfData must include(ViewDates.formatDateAtTime(MrnStatusSpec.completeMrnStatus.receivedDateTime))
          pdfData must include(MrnStatusSpec.completeMrnStatus.versionId)
          pdfData must include(MrnStatusSpec.completeMrnStatus.previousDocuments.head.typeCode)
          pdfData must include(MrnStatusSpec.completeMrnStatus.previousDocuments.head.id)
        } finally {
          pdfDocument.close()
        }
      }
    }

    "should throw an exception" when {
      val mrn = "18GBAKZ81EQJ2FGVR"

      "mrn information is not available" in {
        when(connector.fetchMrnStatus(any[String])(any(), any())).thenReturn(Future.successful(None))

        intercept[IllegalArgumentException](await(service.generatePdf(mrn))).getMessage must be("No declaration information was found")
      }

      "a Runtime exception is thrown" in {
        when(connector.fetchMrnStatus(any[String])(any(), any())).thenReturn(Future.failed(new RuntimeException("some issue")))

        intercept[IllegalArgumentException](await(service.generatePdf(mrn))).getMessage must be("No declaration information was found")
      }
    }
  }
}
