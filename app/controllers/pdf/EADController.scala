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

package controllers.pdf

import java.time.LocalDate

import com.dmanchester.playfop.sapi.PlayFop
import javax.inject.Inject
import org.apache.fop.apps.FOUserAgent
import org.apache.xmlgraphics.util.MimeConstants
import play.api.i18n.I18nSupport
import play.api.mvc.MessagesControllerComponents
import play.twirl.api.XmlFormat
import services.ead.EADService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.xml.pdf.pdfTemplate

class EADController @Inject()(pdfTemplate: pdfTemplate, eadService: EADService)(mcc: MessagesControllerComponents, val playFop: PlayFop)
    extends FrontendController(mcc) with I18nSupport {

  private val mimeType: String = MimeConstants.MIME_PDF

  def generatePdf(mrn: String) = Action { implicit request =>
    {
      val fileName = s"EAD-${mrn}-${LocalDate.now}.pdf"

      val myFOUserAgentBlock = { foUserAgent: FOUserAgent =>
        foUserAgent.setAuthor("HMRC")
      }

      val xml: XmlFormat.Appendable = pdfTemplate.render("Export accompanying document (EAD)", mrn, eadService.base64Image(mrn))

      val pdf: Array[Byte] = playFop.processTwirlXml(xml, mimeType, autoDetectFontsForPDF = true, foUserAgentBlock = myFOUserAgentBlock)

      Ok(pdf).as(mimeType).withHeaders(CONTENT_DISPOSITION -> s"attachment; filename=$fileName")
    }
  }
}
