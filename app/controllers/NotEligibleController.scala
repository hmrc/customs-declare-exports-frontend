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

package controllers

import config.AppConfig
import controllers.actions.AuthAction
import handlers.ErrorHandler
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.not_eligible

import scala.concurrent.Future

class NotEligibleController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticator: AuthAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService
) extends FrontendController with I18nSupport {

  def displayPage(): Action[AnyContent] = authenticator.async { implicit request =>
    Future.successful(Ok(not_eligible(appConfig)))
  }
}