/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.supplementary

import config.AppConfig
import controllers.actions.AuthAction
import forms.supplementary.RepresentativeDetails
import handlers.ErrorHandler
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.supplementary.representative_details

import scala.concurrent.Future

class RepresentativeDetailsPageController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticator: AuthAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService
) extends FrontendController with I18nSupport {

  implicit val countries = services.Countries.allCountries
  private val supplementaryDeclarationCacheId = appConfig.appName

  def displayRepresentativeDetailsPage(): Action[AnyContent] = authenticator.async { implicit request =>
    customsCacheService.fetchAndGetEntry[RepresentativeDetails](
      supplementaryDeclarationCacheId, RepresentativeDetails.formId).map {
        case Some(data) => Ok(representative_details(appConfig, RepresentativeDetails.form().fill(data)))
        case _          => Ok(representative_details(appConfig, RepresentativeDetails.form()))
    }
  }

  def submitRepresentativeDetails(): Action[AnyContent] = authenticator.async { implicit request =>
    RepresentativeDetails.form().bindFromRequest().fold(
      (formWithErrors: Form[RepresentativeDetails]) =>
        Future.successful(BadRequest(representative_details(appConfig, formWithErrors))),
      validRepresentativeDetails =>
        customsCacheService.cache[RepresentativeDetails](
          supplementaryDeclarationCacheId,
          RepresentativeDetails.formId,
          validRepresentativeDetails
        ).map { _ =>
          Redirect(controllers.supplementary.routes.DeclarationAdditionalActorsController.displayForm())
        }
    )
  }

}
