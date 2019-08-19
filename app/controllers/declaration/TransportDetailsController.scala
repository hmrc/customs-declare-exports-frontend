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
import forms.Choice.AllowedChoiceValues
import forms.declaration.TransportDetails
import forms.declaration.TransportDetails._
import javax.inject.Inject
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.transport_details

import scala.concurrent.{ExecutionContext, Future}

class TransportDetailsController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  transportDetailsPage: transport_details
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.transportDetails match {
      case Some(data) => Ok(transportDetailsPage(form.fill(data)))
      case _          => Ok(transportDetailsPage(form))
    }
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[TransportDetails]) => Future.successful(BadRequest(transportDetailsPage(formWithErrors))),
        transportDetails => updateCache(journeySessionId, transportDetails).map(_ => redirect(transportDetails))
      )
  }

  private def redirect(transportDetails: TransportDetails)(implicit request: JourneyRequest[_]): Result =
    if (transportDetails.container) Redirect(routes.TransportContainerController.displayPage())
    else if (request.choice.value == AllowedChoiceValues.StandardDec) Redirect(routes.SealController.displayForm())
    else Redirect(routes.SummaryController.displayPage(Mode.NormalMode))

  private def updateCache(sessionId: String, formData: TransportDetails): Future[Option[ExportsDeclaration]] =
    getAndUpdateExportsDeclaration(
      sessionId,
      model => exportsCacheService.update(sessionId, model.copy(transportDetails = Some(formData)))
    )
}
