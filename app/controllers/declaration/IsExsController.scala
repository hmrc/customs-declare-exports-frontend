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

package controllers.declaration

import controllers.actions.{AuthAction, JourneyAction}
import controllers.navigation.Navigator
import forms.DeclarationPage
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.{ExporterDetails, IsExs}
import javax.inject.Inject
import models.DeclarationType.CLEARANCE
import models.DeclarationType.DeclarationType
import models.{ExportsDeclaration, Mode}
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.is_exs

import scala.concurrent.{ExecutionContext, Future}

class IsExsController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  isExsPage: is_exs
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  private val allowedJourney: DeclarationType = CLEARANCE

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType(allowedJourney)) { implicit request =>
    request.cacheModel.parties.isExs match {
      case Some(data) => Ok(isExsPage(mode, navigationPage, IsExs.form.fill(data)))
      case _          => Ok(isExsPage(mode, navigationPage, IsExs.form))
    }
  }

  def submit(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType(allowedJourney)).async { implicit request =>
    IsExs.form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(isExsPage(mode, navigationPage, formWithErrors))),
        answer => updateCache(answer).map(_ => navigator.continueTo(mode, nextPage(answer)))
      )
  }

  private def navigationPage(implicit request: JourneyRequest[_]): DeclarationPage =
    if (request.cacheModel.isDeclarantExporter) ExporterDetails else IsExs

  private def updateCache(answer: IsExs)(implicit request: JourneyRequest[_]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      val updatedParties = model.parties.copy(isExs = Some(answer))
      model.copy(parties = updatedParties)
    })

  private def nextPage(isExs: IsExs)(implicit request: JourneyRequest[_]): Mode => Call =
    isExs.isExs match {
      case YesNoAnswers.yes => controllers.declaration.routes.ConsignorEoriNumberController.displayPage
      case YesNoAnswers.no =>
        if (request.cacheModel.isDeclarantExporter) controllers.declaration.routes.ConsigneeDetailsController.displayPage
        else controllers.declaration.routes.RepresentativeAgentController.displayPage
    }
}
