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

import controllers.actions.{AuthAction, JourneyAction}
import controllers.navigation.Navigator
import forms.declaration.SupervisingCustomsOffice
import javax.inject.Inject
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.supervising_customs_office

import scala.concurrent.{ExecutionContext, Future}

class SupervisingCustomsOfficeController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  supervisingCustomsOfficePage: supervising_customs_office
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  import forms.declaration.SupervisingCustomsOffice._

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.locations.supervisingCustomsOffice match {
      case Some(data) => Ok(supervisingCustomsOfficePage(mode, form().fill(data)))
      case _          => Ok(supervisingCustomsOfficePage(mode, form()))
    }
  }

  def submit(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(supervisingCustomsOfficePage(mode, formWithErrors))),
        form => {
          updateCache(form)
            .map(_ => navigator.continueTo(nextPage(mode, request)))
        }
      )
  }

  private def nextPage(mode: Mode, request: JourneyRequest[AnyContent]) =
    request.declarationType match {
      case DeclarationType.SUPPLEMENTARY | DeclarationType.STANDARD =>
        controllers.declaration.routes.InlandTransportDetailsController.displayPage(mode)
      case DeclarationType.SIMPLIFIED | DeclarationType.OCCASIONAL=>
        controllers.declaration.routes.TransportPaymentController.displayPage(mode)
    }

  private def updateCache(formData: SupervisingCustomsOffice)(implicit request: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => model.copy(locations = model.locations.copy(supervisingCustomsOffice = Some(formData))))
}
