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
import controllers.navigation.Navigator
import forms.declaration.officeOfExit.OfficeOfExit
import forms.declaration.officeOfExit.OfficeOfExit.form
import models.DeclarationType.DeclarationType
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.data.Form
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
    val frm = form().withSubmissionErrors()
    request.cacheModel.locations.officeOfExit match {
      case Some(data) =>
        Ok(officeOfExitPage(mode, frm.fill(data)))
      case _ => Ok(officeOfExitPage(mode, frm))
    }
  }

  def saveOffice(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[OfficeOfExit]) => {
          val formWithAdjustedErrors = formWithErrors

          Future.successful(BadRequest(officeOfExitPage(mode, formWithAdjustedErrors)))
        },
        form =>
          updateCache(form, request.cacheModel.locations.officeOfExit)
            .map(_ => navigator.continueTo(mode, nextPage(request.declarationType)))
      )
  }

  private def nextPage(declarationType: DeclarationType): Mode => Call =
    declarationType match {
      case DeclarationType.SUPPLEMENTARY | DeclarationType.STANDARD =>
        controllers.declaration.routes.TotalNumberOfItemsController.displayPage
      case DeclarationType.SIMPLIFIED | DeclarationType.OCCASIONAL | DeclarationType.CLEARANCE =>
        controllers.declaration.routes.PreviousDocumentsSummaryController.displayPage
    }

  private def updateCache(formData: OfficeOfExit, officeOfExit: Option[OfficeOfExit])(
    implicit r: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(model => model.copy(locations = model.locations.copy(officeOfExit = Some(formData))))
}
