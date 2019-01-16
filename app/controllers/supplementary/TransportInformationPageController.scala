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

package controllers.supplementary

import config.AppConfig
import controllers.actions.AuthAction
import controllers.helpers.ControllersHelper._
import forms.supplementary.TransportInformation
import handlers.ErrorHandler
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.supplementary.transport_information

import scala.concurrent.{ExecutionContext, Future}

class TransportInformationPageController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  implicit val countries = services.Countries.allCountries
  private val supplementaryDeclarationCacheId = appConfig.appName

  def displayPage(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService
      .fetchAndGetEntry[TransportInformation](supplementaryDeclarationCacheId, TransportInformation.id)
      .map {
        case Some(data) => Ok(transport_information(appConfig, TransportInformation.form.fill(data)))
        case _          => Ok(transport_information(appConfig, TransportInformation.form))
      }
  }

  def submitTransportInformation(): Action[AnyContent] = authenticate.async { implicit request =>
    TransportInformation.form
      .bindFromRequest(trimFormData(request))
      .fold(
        (formWithErrors: Form[TransportInformation]) =>
          Future.successful(BadRequest(transport_information(appConfig, formWithErrors))),
        validTransportInformation =>
          customsCacheService
            .cache[TransportInformation](
              supplementaryDeclarationCacheId,
              TransportInformation.id,
              validTransportInformation
            )
            .map { _ =>
              Redirect(controllers.supplementary.routes.TotalNumberOfItemsController.displayForm())
          }
      )
  }

}
