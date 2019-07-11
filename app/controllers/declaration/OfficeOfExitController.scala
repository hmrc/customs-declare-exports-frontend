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
import config.AppConfig
import controllers.actions.{AuthAction, JourneyAction}
import controllers.util.CacheIdGenerator.cacheId
import forms.Choice.AllowedChoiceValues.{StandardDec, SupplementaryDec}
import forms.declaration.officeOfExit.{OfficeOfExit, OfficeOfExitStandard, OfficeOfExitSupplementary}
import javax.inject.Inject
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.twirl.api.Html
import services.CustomsCacheService
import services.cache.{ExportsCacheModel, ExportsCacheService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.{office_of_exit_standard, office_of_exit_supplementary}

import scala.concurrent.{ExecutionContext, Future}

class OfficeOfExitController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  customsCacheService: CustomsCacheService,
  mcc: MessagesControllerComponents,
  officeOfExitSupplementaryPage: office_of_exit_supplementary,
  officeOfExitStandardPage: office_of_exit_standard,
  override val cacheService: ExportsCacheService
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {
  import forms.declaration.officeOfExit.OfficeOfExitForms._

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    request.choice.value match {
      case SupplementaryDec => supplementaryPage.map(Ok(_))
      case StandardDec      => standardPage.map(Ok(_))
    }
  }

  private def supplementaryPage()(implicit request: JourneyRequest[_], hc: HeaderCarrier): Future[Html] =
    cacheService.get(journeySessionId).map(_.flatMap(_.locations.officeOfExit)).map {
      case Some(data) => officeOfExitSupplementaryPage(supplementaryForm.fill(OfficeOfExitSupplementary(data)))
      case _          => officeOfExitSupplementaryPage(supplementaryForm)
    }

  private def standardPage()(implicit request: JourneyRequest[_], hc: HeaderCarrier): Future[Html] =
    cacheService.get(journeySessionId).map(_.flatMap(_.locations.officeOfExit)).map {
      case Some(data) => officeOfExitStandardPage(standardForm.fill(OfficeOfExitStandard(data)))
      case _          => officeOfExitStandardPage(standardForm)
    }

  def saveOffice(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    request.choice.value match {
      case SupplementaryDec => saveSupplementaryOffice()
      case StandardDec      => saveStandardOffice()
    }
  }

  private def saveSupplementaryOffice()(implicit request: JourneyRequest[_]): Future[Result] =
    supplementaryForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[OfficeOfExitSupplementary]) =>
          Future.successful(BadRequest(officeOfExitSupplementaryPage(formWithErrors))),
        form =>
          for {
            _ <- updateCache(journeySessionId, form)
            _ <- customsCacheService.cache[OfficeOfExitSupplementary](cacheId, formId, form)
          } yield Redirect(controllers.declaration.routes.TotalNumberOfItemsController.displayForm())
      )

  private def saveStandardOffice()(implicit request: JourneyRequest[_]): Future[Result] =
    standardForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[OfficeOfExitStandard]) => {
          val formWithAdjustedErrors = OfficeOfExitStandard.adjustCircumstancesError(formWithErrors)

          Future.successful(BadRequest(officeOfExitStandardPage(formWithAdjustedErrors)))
        },
        form =>
          for {
            _ <- updateCache(journeySessionId, form)
            _ <- customsCacheService.cache[OfficeOfExitStandard](cacheId, formId, form)
          } yield Redirect(controllers.declaration.routes.TotalNumberOfItemsController.displayForm())
      )

  private def updateCache(sessionId: String, formData: OfficeOfExitSupplementary): Future[Option[ExportsCacheModel]] =
    getAndUpdateExportCacheModel(
      sessionId,
      model =>
        cacheService.update(
          sessionId,
          model.copy(locations = model.locations.copy(officeOfExit = Some(OfficeOfExit.from(formData))))
      )
    )

  private def updateCache(sessionId: String, formData: OfficeOfExitStandard): Future[Option[ExportsCacheModel]] =
    getAndUpdateExportCacheModel(
      sessionId,
      model =>
        cacheService.update(
          sessionId,
          model.copy(locations = model.locations.copy(officeOfExit = Some(OfficeOfExit.from(formData))))
      )
    )
}
