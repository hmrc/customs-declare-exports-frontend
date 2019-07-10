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
import forms.declaration.DeclarantDetails
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CustomsCacheService
import services.cache.{ExportsCacheModel, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.declarant_details

import scala.concurrent.{ExecutionContext, Future}

class DeclarantDetailsPageController @Inject()(
  appConfig: AppConfig,
  authenticate: AuthAction,
  journeyType: JourneyAction,
  customsCacheService: CustomsCacheService,
  exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  declarantDetailsPage: declarant_details
)(implicit ec: ExecutionContext)
    extends {
  val cacheService = exportsCacheService
} with FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService.fetchAndGetEntry[DeclarantDetails](cacheId, DeclarantDetails.id).map {
      case Some(data) => Ok(declarantDetailsPage(appConfig, DeclarantDetails.form.fill(data)))
      case _          => Ok(declarantDetailsPage(appConfig, DeclarantDetails.form))
    }
  }

  def saveAddress(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    DeclarantDetails.form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[DeclarantDetails]) =>
          Future.successful(BadRequest(declarantDetailsPage(appConfig, formWithErrors))),
        form =>
          for {
            _ <- updateCache(journeySessionId, form)
            _ <- customsCacheService.cache[DeclarantDetails](cacheId, DeclarantDetails.id, form)
          } yield
            Redirect(
              controllers.declaration.routes.RepresentativeDetailsPageController.displayRepresentativeDetailsPage()
          )
      )
  }

  private def updateCache(sessionId: String, formData: DeclarantDetails): Future[Option[ExportsCacheModel]] =
    getAndUpdateExportCacheModel(sessionId, model => {
      val updatedParties = model.parties.copy(declarantDetails = Some(formData))
      exportsCacheService.update(sessionId, model.copy(parties = updatedParties))
    })
}
