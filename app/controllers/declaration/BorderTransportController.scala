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
import controllers.declaration.routes.TransportDetailsController
import controllers.util.CacheIdGenerator.cacheId
import forms.declaration.BorderTransport._
import forms.declaration.{BorderTransport, FiscalInformation}
import handlers.ErrorHandler
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.border_transport

import scala.concurrent.ExecutionContext

class BorderTransportController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService,
  mcc: MessagesControllerComponents
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc) with I18nSupport {

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService
      .fetchAndGetEntry[BorderTransport](cacheId, BorderTransport.formId)
      .flatMap { borderTransportData =>
        customsCacheService
          .fetchAndGetEntry[FiscalInformation](cacheId, FiscalInformation.formId)
          .map(
            data =>
              Ok(
                border_transport(
                  borderTransportData.fold(form)(form.fill(_)),
                  data.fold(false)(_.onwardSupplyRelief == "Yes")
                )
            )
          )
      }
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[BorderTransport]) =>
          customsCacheService
            .fetchAndGetEntry[FiscalInformation](cacheId, FiscalInformation.formId)
            .map(data => BadRequest(border_transport(formWithErrors, data.fold(false)(_.onwardSupplyRelief == "Yes")))),
        borderTransport =>
          customsCacheService
            .cache[BorderTransport](cacheId, BorderTransport.formId, borderTransport)
            .map(_ => Redirect(TransportDetailsController.displayForm()))
      )
  }

}
