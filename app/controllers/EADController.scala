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

package controllers

import controllers.actions.AuthAction
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ead.{BarcodeService, EADService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.ead

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class EADController @Inject() (
  authenticate: AuthAction,
  mcc: MessagesControllerComponents,
  eadService: EADService,
  barcodeService: BarcodeService,
  ead_page: ead
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def generateDocument(mrn: String): Action[AnyContent] = authenticate.async { implicit request =>
    eadService.generateStatus(mrn) map { status =>
      Ok(ead_page(mrn, status, barcodeService.base64Image(mrn)))
    }
  }
}
