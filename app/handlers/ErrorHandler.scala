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

package handlers

import config.AppConfig
import controllers.routes
import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Request, RequestHeader, Result, Results}
import play.api.mvc.Results.BadRequest
import play.api.{Configuration, Environment}
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.{InsufficientEnrolments, NoActiveSession}
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects
import uk.gov.hmrc.play.bootstrap.http.FrontendErrorHandler

import scala.concurrent.Future

@Singleton
class ErrorHandler @Inject()(appConfig: AppConfig, val messagesApi: MessagesApi)
    extends FrontendErrorHandler with I18nSupport with AuthRedirects {
  override def config: Configuration = appConfig.runModeConfiguration

  override def env: Environment = appConfig.environment

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(
    implicit request: Request[_]
  ): Html =
    views.html.error_template(pageTitle, heading, message, appConfig)

  override def resolveError(rh: RequestHeader, ex: Throwable): Result = ex match {
    case _: NoActiveSession        => toGGLogin(rh.uri)
    case _: InsufficientEnrolments => Results.SeeOther(routes.UnauthorisedController.onPageLoad().url)
    case _                         => super.resolveError(rh, ex)
  }

  def displayErrorPage()(implicit request: Request[_]): Future[Result] =
    Future.successful(
      BadRequest(
        standardErrorTemplate(
          pageTitle = messagesApi("global.error.title"),
          heading = messagesApi("global.error.heading"),
          message = messagesApi("global.error.message")
        )
      )
    )

  def globalErrorTemplate()(implicit request: Request[_]): Html =
    views.html.error_template(
      pageTitle = messagesApi("global.error.title"),
      heading = messagesApi("global.error.heading"),
      message = messagesApi("global.error.message"),
      appConfig = appConfig
    )
}
