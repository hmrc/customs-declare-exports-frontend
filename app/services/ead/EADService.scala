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

package services.ead

import com.dmanchester.playfop.sapi.PlayFop
import connectors.ead.CustomsDeclarationsInformationConnector
import javax.inject.Inject
import org.apache.fop.apps.FOUserAgent
import org.apache.xmlgraphics.util.MimeConstants
import play.twirl.api.XmlFormat
import uk.gov.hmrc.http.HeaderCarrier
import views.xml.pdf.pdfTemplate

import scala.concurrent.{ExecutionContext, Future}

class EADService @Inject()(
  barcodeService: BarcodeService,
  pdfTemplate: pdfTemplate,
  val playFop: PlayFop,
  connector: CustomsDeclarationsInformationConnector
) {

  def generatePdf(mrn: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Array[Byte]] = {
    val myFOUserAgentBlock = { foUserAgent: FOUserAgent =>
      foUserAgent.setAuthor("HMRC")
    }

    connector
      .fetchMrnStatus(mrn)
      .map(mrnStatus => {
        val xml: XmlFormat.Appendable = pdfTemplate.render("Export accompanying document (EAD)", mrnStatus, barcodeService.base64Image(mrn))

        playFop.processTwirlXml(xml, MimeConstants.MIME_PDF, autoDetectFontsForPDF = true, foUserAgentBlock = myFOUserAgentBlock)
      })
  }
}
