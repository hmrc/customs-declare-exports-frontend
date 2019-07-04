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
import forms.declaration.DispatchLocation
import forms.declaration.DispatchLocation.AllowedDispatchLocations
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.CustomsCacheService
import services.cache.{ExportsCacheModel, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.dispatch_location

import scala.concurrent.{ExecutionContext, Future}

class DispatchLocationPageController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  customsCacheService: CustomsCacheService,
  exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents
)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends {
  val cacheService = exportsCacheService
} with FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService
      .fetchAndGetEntry[DispatchLocation](cacheId, DispatchLocation.formId)
      .map {
        case Some(data) => Ok(dispatch_location(DispatchLocation.form().fill(data)))
        case _          => Ok(dispatch_location(DispatchLocation.form()))
      }
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    DispatchLocation
      .form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[DispatchLocation]) => Future.successful(BadRequest(dispatch_location(formWithErrors))),
        validDispatchLocation => {
          for {
            _ <- updateCache(journeySessionId, validDispatchLocation)
            _ <- customsCacheService.cache[DispatchLocation](cacheId, DispatchLocation.formId, validDispatchLocation)
          } yield Redirect(specifyNextPage(validDispatchLocation))
        }
      )
  }

  private def specifyNextPage(providedDispatchLocation: DispatchLocation): Call =
    providedDispatchLocation.dispatchLocation match {
      case AllowedDispatchLocations.OutsideEU =>
        controllers.declaration.routes.AdditionalDeclarationTypePageController.displayPage()
      case AllowedDispatchLocations.SpecialFiscalTerritory =>
        controllers.declaration.routes.NotEligibleController.displayPage()
    }

  private def updateCache(sessionId: String, formData: DispatchLocation): Future[Either[String, ExportsCacheModel]] =
    updateHeaderLevelCache(
      sessionId,
      model => exportsCacheService.update(sessionId, model.copy(dispatchLocation = Some(formData)))
    )
}
