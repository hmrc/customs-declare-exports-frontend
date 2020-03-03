/*
 * Copyright 2020 HM Revenue & Customs
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

package unit.controllers.pdf

import java.io.ByteArrayInputStream

import base.Injector
import com.dmanchester.playfop.sapi.PlayFop
import controllers.pdf.EADController
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.krysalis.barcode4j.impl.code128.Code128Bean
import play.api.test.Helpers._
import services.ead.EADService
import unit.base.ControllerSpec
import views.xml.pdf.pdfTemplate

class EADControllerSpec extends ControllerSpec with Injector {

  val mcc = stubMessagesControllerComponents()
  val pdfTemplate = new pdfTemplate()
  val playFop = instanceOf[PlayFop]
  val eADService = instanceOf[EADService]

  val controller = new EADController(pdfTemplate, eADService)(mcc, playFop)

  "EAD Controller" should {

    "return 200" when {

      "display page method is invoked" in {

        val mrn = "20GB0NNR0WK39FGV09"
        val result = controller.generatePdf(mrn).apply(getRequest())

        status(result) must be(OK)

        val pdfDocument = PDDocument.load(new ByteArrayInputStream(contentAsBytes(result).toArray))

        try {
          val pdfData = new PDFTextStripper().getText(pdfDocument)
          pdfData must include(mrn)
        } finally {
          pdfDocument.close()
        }
      }
    }
  }
}
