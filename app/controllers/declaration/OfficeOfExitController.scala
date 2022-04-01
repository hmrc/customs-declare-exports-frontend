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

package controllers.declaration

import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.routes.{PreviousDocumentsSummaryController, InvoiceAndExchangeRateChoiceController}
import controllers.navigation.Navigator
import forms.declaration.officeOfExit.OfficeOfExit
import forms.declaration.officeOfExit.OfficeOfExit.form
import models.DeclarationType._
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.office_of_exit

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OfficeOfExitController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  officeOfExitPage: office_of_exit,
  override val exportsCacheService: ExportsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.locations.officeOfExit match {
      case Some(data) => Ok(officeOfExitPage(mode, form.withSubmissionErrors.fill(data)))
      case _          => Ok(officeOfExitPage(mode, form.withSubmissionErrors))
    }
  }

  def saveOffice(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form.bindFromRequest
      .fold(
        formWithErrors => Future.successful(BadRequest(officeOfExitPage(mode, formWithErrors))),
        updateCache(_).map(_ => navigator.continueTo(mode, nextPage(request.declarationType)))
      )
  }

  private def nextPage(declarationType: DeclarationType): Mode => Call =
    declarationType match {
      case SUPPLEMENTARY | STANDARD            => InvoiceAndExchangeRateChoiceController.displayPage
      case SIMPLIFIED | OCCASIONAL | CLEARANCE => PreviousDocumentsSummaryController.displayPage
    }

  private def updateCache(officeOfExit: OfficeOfExit)(implicit r: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(model => model.copy(locations = model.locations.copy(officeOfExit = Some(officeOfExit))))
}
