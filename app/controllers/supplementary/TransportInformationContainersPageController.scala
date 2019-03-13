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
import controllers.util.CacheIdGenerator.supplementaryCacheId
import forms.supplementary.TransportInformationContainer.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.supplementary.TransportInformationContainerData
import models.declaration.supplementary.TransportInformationContainerData.formId
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.supplementary.add_transport_containers

import scala.concurrent.ExecutionContext

class TransportInformationContainersPageController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  implicit val countries = services.Countries.allCountries

  def displayPage(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService
      .fetchAndGetEntry[TransportInformationContainerData](supplementaryCacheId, formId)
      .map {
        case Some(data) => Ok(add_transport_containers(appConfig, form, data.containers))
        case _          => Ok(add_transport_containers(appConfig, form, Seq()))
      }
  }

  def submitTransportInformation(): Action[AnyContent] = ???

}
