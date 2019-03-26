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
import forms.declaration.RepresentativeDetails
import handlers.ErrorHandler
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.representative_details

import scala.concurrent.{ExecutionContext, Future}

class RepresentativeDetailsPageController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  implicit val countries = services.Countries.allCountries

  def displayRepresentativeDetailsPage(): Action[AnyContent] = (authenticate andThen journeyType).async {
    implicit request =>
      customsCacheService
        .fetchAndGetEntry[RepresentativeDetails](cacheId, RepresentativeDetails.formId)
        .map {
          case Some(data) => Ok(representative_details(appConfig, RepresentativeDetails.form.fill(data)))
          case _          => Ok(representative_details(appConfig, RepresentativeDetails.form))
        }
  }

  def submitRepresentativeDetails(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    RepresentativeDetails.form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[RepresentativeDetails]) =>
          Future.successful(BadRequest(representative_details(appConfig, formWithErrors))),
        validRepresentativeDetails =>
          customsCacheService
            .cache[RepresentativeDetails](cacheId, RepresentativeDetails.formId, validRepresentativeDetails)
            .map(_ => Redirect(controllers.declaration.routes.DeclarationAdditionalActorsController.displayForm()))
      )
  }

}
