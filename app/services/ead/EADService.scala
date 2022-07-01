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

import scala.concurrent.{ExecutionContext, Future}

import com.dmanchester.playfop.sapi.PlayFop
import connectors.CustomsDeclareExportsConnector
import javax.inject.Inject
import org.apache.fop.apps.FOUserAgent
import org.apache.xmlgraphics.util.MimeConstants
import play.api.Logging
import play.api.i18n.Messages
import play.twirl.api.XmlFormat
import uk.gov.hmrc.http.HeaderCarrier
import views.xml.pdf.pdfTemplate

class EADService @Inject() (barcodeService: BarcodeService, pdfTemplate: pdfTemplate, val playFop: PlayFop, connector: CustomsDeclareExportsConnector)
    extends Logging {

  def generatePdf(mrn: String)(implicit hc: HeaderCarrier, ec: ExecutionContext, messages: Messages): Future[Array[Byte]] = {
    val myFOUserAgentBlock = { foUserAgent: FOUserAgent =>
      foUserAgent.setAuthor("HMRC")
    }

    connector
      .fetchMrnStatus(mrn)
      .map {
        case Some(mrnStatus) =>
          val xml: XmlFormat.Appendable = pdfTemplate.render(mrn, mrnStatus, barcodeService.base64Image(mrn), messages)

          playFop.processTwirlXml(xml, MimeConstants.MIME_PDF, autoDetectFontsForPDF = true, foUserAgentBlock = myFOUserAgentBlock)
        case _ => throw new IllegalArgumentException(s"No declaration information was found")
      }
      .recoverWith { case _: Throwable =>
        logger.error("An error occurred whilst trying to retrieve mrn status")
        throw new IllegalArgumentException(s"No declaration information was found")
      }
  }
}
