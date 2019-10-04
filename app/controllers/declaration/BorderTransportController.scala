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
import forms.declaration.BorderTransport
import forms.declaration.BorderTransport._
import javax.inject.Inject
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.border_transport

import scala.concurrent.{ExecutionContext, Future}

class BorderTransportController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  borderTransport: border_transport
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.transportDetails match {
      case Some(data) => Ok(borderTransport(mode, form().fill(data)))
      case _          => Ok(borderTransport(mode, form()))
    }
  }

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[BorderTransport]) => Future.successful(BadRequest(borderTransport(mode, formWithErrors))),
        transportDetails => updateCache(transportDetails).map(_ => redirect(mode, transportDetails))
      )
  }

  private def redirect(mode: Mode, transportDetails: BorderTransport)(
    implicit request: JourneyRequest[AnyContent]
  ): Result =
    if (transportDetails.container)
      navigator.continueTo(controllers.declaration.routes.TransportContainerController.displayContainerSummary(mode))
    else navigator.continueTo(controllers.declaration.routes.SummaryController.displayPage(Mode.Normal))

  private def updateCache(
    formData: BorderTransport
  )(implicit r: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => model.copy(transportDetails = Some(formData)))
}
