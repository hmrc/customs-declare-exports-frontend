/*
 * Copyright 2023 HM Revenue & Customs
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
import models.UnauthorisedReason.{UserIsAgent, UserIsNotEnrolled}

import javax.inject.{Inject, Singleton}
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Results.BadRequest
import play.api.mvc.{Request, RequestHeader, Result, Results}
import play.api.{Configuration, Environment}
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.{InsufficientEnrolments, NoActiveSession, UnsupportedAffinityGroup}
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import views.html.error_template

import scala.concurrent.Future

@Singleton
class ErrorHandler @Inject() (override val messagesApi: MessagesApi, errorPage: error_template)(implicit appConfig: AppConfig)
    extends FrontendErrorHandler with AuthRedirects {
  override def config: Configuration = appConfig.runModeConfiguration

  override def env: Environment = appConfig.environment

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_]): Html =
    errorPage(pageTitle, heading, message)

  override def resolveError(rh: RequestHeader, ex: Throwable): Result = ex match {
    case _: NoActiveSession          => Results.Redirect(appConfig.loginUrl, Map("continue" -> Seq(appConfig.loginContinueUrl)))
    case _: InsufficientEnrolments   => Results.SeeOther(routes.UnauthorisedController.onPageLoad(UserIsNotEnrolled).url)
    case _: UnsupportedAffinityGroup => Results.Redirect(routes.UnauthorisedController.onAgentKickOut(UserIsAgent))
    case _                           => super.resolveError(rh, ex)
  }

  def globalErrorPage(implicit request: Request[_]): Html =
    standardErrorTemplate(
      pageTitle = Messages("global.error.title"),
      heading = Messages("global.error.heading"),
      message = Messages("global.error.message")
    )

  def displayErrorPage(implicit request: Request[_]): Future[Result] =
    Future.successful(BadRequest(globalErrorPage))
}
