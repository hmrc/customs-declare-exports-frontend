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
import forms.Choice.AllowedChoiceValues.{StandardDec, SupplementaryDec}
import forms.declaration.officeOfExit.{OfficeOfExit, OfficeOfExitStandard, OfficeOfExitSupplementary}
import javax.inject.Inject
import models.{ExportsDeclaration, Mode}
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.twirl.api.Html
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.{office_of_exit_standard, office_of_exit_supplementary}

import scala.concurrent.{ExecutionContext, Future}

class OfficeOfExitController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  officeOfExitSupplementaryPage: office_of_exit_supplementary,
  officeOfExitStandardPage: office_of_exit_standard,
  override val exportsCacheService: ExportsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {
  import forms.declaration.officeOfExit.OfficeOfExitForms._

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.choice.value match {
      case SupplementaryDec => Ok(supplementaryPage(mode))
      case StandardDec      => Ok(standardPage(mode))
    }
  }

  private def supplementaryPage(mode: Mode)(implicit request: JourneyRequest[_]): Html =
    request.cacheModel.locations.officeOfExit match {
      case Some(data) => officeOfExitSupplementaryPage(mode, supplementaryForm().fill(OfficeOfExitSupplementary(data)))
      case _          => officeOfExitSupplementaryPage(mode, supplementaryForm())
    }

  private def standardPage(mode: Mode)(implicit request: JourneyRequest[_], hc: HeaderCarrier): Html =
    request.cacheModel.locations.officeOfExit match {
      case Some(data) => officeOfExitStandardPage(mode, standardForm().fill(OfficeOfExitStandard(data)))
      case _          => officeOfExitStandardPage(mode, standardForm())
    }

  def saveOffice(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    request.choice.value match {
      case SupplementaryDec => saveSupplementaryOffice(mode)
      case StandardDec      => saveStandardOffice(mode)
    }
  }

  private def saveSupplementaryOffice(mode: Mode)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    supplementaryForm()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[OfficeOfExitSupplementary]) =>
          Future.successful(BadRequest(officeOfExitSupplementaryPage(mode, formWithErrors))),
        form =>
          updateCache(form)
            .map(
              _ => navigator.continueTo(controllers.declaration.routes.TotalNumberOfItemsController.displayPage(mode))
          )
      )

  private def saveStandardOffice(mode: Mode)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    standardForm()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[OfficeOfExitStandard]) => {
          val formWithAdjustedErrors = OfficeOfExitStandard.adjustCircumstancesError(formWithErrors)

          Future.successful(BadRequest(officeOfExitStandardPage(mode, formWithAdjustedErrors)))
        },
        form =>
          updateCache(form)
            .map(
              _ => navigator.continueTo(controllers.declaration.routes.TotalNumberOfItemsController.displayPage(mode))
          )
      )

  private def updateCache(
    formData: OfficeOfExitSupplementary
  )(implicit r: JourneyRequest[_]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(
      model => model.copy(locations = model.locations.copy(officeOfExit = Some(OfficeOfExit.from(formData))))
    )

  private def updateCache(
    formData: OfficeOfExitStandard
  )(implicit r: JourneyRequest[_]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(
      model => model.copy(locations = model.locations.copy(officeOfExit = Some(OfficeOfExit.from(formData))))
    )
}
