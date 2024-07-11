/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.timeline

import connectors.CustomsDeclareExportsConnector
import controllers.actions.AuthAction
import controllers.general.ErrorHandler
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ead.BarcodeService
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.timeline.ead

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class EADController @Inject() (
  authenticate: AuthAction,
  mcc: MessagesControllerComponents,
  connector: CustomsDeclareExportsConnector,
  errorHandler: ErrorHandler,
  barcodeService: BarcodeService,
  ead_page: ead
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with Logging {

  def generateDocument(mrn: String): Action[AnyContent] = authenticate.async { implicit request =>
    connector
      .fetchMrnStatus(mrn)
      .map { mrnStatus =>
        Ok(ead_page(mrn, mrnStatus, barcodeService.base64Image(mrn)))
      }
      .recoverWith { case UpstreamErrorResponse(_, NOT_FOUND, _, _) =>
        logger.error(s"No declaration was found whilst trying to retrieve status for the EAD page with MRN($mrn)")
        errorHandler.redirectToErrorPage
      }
  }
}
