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

package controllers.pdf

import java.io.ByteArrayInputStream

import scala.concurrent.Future

import base.{ControllerWithoutFormSpec, Injector}
import com.dmanchester.playfop.sapi.PlayFop
import connectors.CustomsDeclareExportsConnector
import models.dis.MrnStatusSpec
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.Helpers._
import services.ead.{BarcodeService, EADService}
import views.helpers.ViewDates
import views.xml.pdf.pdfTemplate

class EADControllerSpec extends ControllerWithoutFormSpec with Injector {

  val mcc = stubMessagesControllerComponents()
  val barcodeService = instanceOf[BarcodeService]
  val playFop = instanceOf[PlayFop]
  val pdfTemplate = new pdfTemplate()
  val connector = mock[CustomsDeclareExportsConnector]
  val eADService = new EADService(barcodeService, pdfTemplate, playFop, connector)

  val controller = new EADController(mockAuthAction, mcc, eADService)

  override def beforeEach(): Unit = {
    when(connector.fetchMrnStatus(any())(any(), any())).thenReturn(Future.successful(Some(MrnStatusSpec.completeMrnStatus)))
    super.beforeEach()
    authorizedUser()
  }

  override protected def afterEach(): Unit =
    super.afterEach()

  "EAD Controller" should {

    "return 200" when {

      "display page method is invoked" in {

        val mrn = "18GB9JLC3CU1LFGVR2"
        val result = controller.generatePdf(mrn).apply(getRequest())

        status(result) must be(OK)

        val pdfDocument = PDDocument.load(new ByteArrayInputStream(contentAsBytes(result).toArray))

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
        } finally
          pdfDocument.close()
      }
    }
  }
}
