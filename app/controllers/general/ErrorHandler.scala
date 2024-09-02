/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.general

import config.AppConfig
import controllers.general.routes.UnauthorisedController
import models.UnauthorisedReason.{UserIsAgent, UserIsNotEnrolled}
import play.api.Logging
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.i18n.MessagesApi
import play.api.mvc.Results.{BadRequest, InternalServerError}
import play.api.mvc.{Request, RequestHeader, Result, Results}
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.{InsufficientEnrolments, NoActiveSession, UnsupportedAffinityGroup}
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import views.html.general.error_template

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ErrorHandler @Inject() (
  override val messagesApi: MessagesApi,
  errorTemplate: error_template
)(implicit appConfig: AppConfig, executionContext: ExecutionContext)  extends FrontendErrorHandler with Logging {

  implicit val ec: ExecutionContext = executionContext

  override def standardErrorTemplate(titleKey: String, headingKey: String, messageKey: String)(
    implicit requestHeader: RequestHeader
  ): Future[Html] = {
    implicit val request: Request[_] = Request(requestHeader, "")
    Future.successful(defaultErrorTemplate(titleKey, headingKey, messageKey))
  }

  override def resolveError(rh: RequestHeader, ex: Throwable): Future[Result] = {
    val result = ex match {
      case _: NoActiveSession          => Results.Redirect(appConfig.loginUrl, Map("continue" -> Seq(appConfig.loginContinueUrl)))
      case _: InsufficientEnrolments   => Results.SeeOther(UnauthorisedController.onPageLoad(UserIsNotEnrolled).url)
      case _: UnsupportedAffinityGroup => Results.Redirect(UnauthorisedController.onAgentKickOut(UserIsAgent))
      case _                           => internalServerError(ex.getMessage)(Request(rh, ""))
    }
    Future.successful(result)
  }

  def defaultErrorTemplate(
    titleKey: String = "global.error.title",
    headingKey: String = "global.error.heading",
    messageKey: String = "global.error.message"
  )(implicit request: Request[_]): Html = errorTemplate(titleKey, headingKey, messageKey)

  def badRequest(implicit request: Request[_]): Result =
    BadRequest(defaultErrorTemplate()).withHeaders(CACHE_CONTROL -> "no-cache")

  def internalServerError(message: String)(implicit request: Request[_]): Result = {
    logger.warn(message)
    InternalServerError(defaultErrorTemplate()).withHeaders(CACHE_CONTROL -> "no-cache")
  }

  def internalError(message: String)(implicit request: Request[_]): Future[Result] =
    Future.successful(internalServerError(message))

  def redirectToErrorPage(implicit request: Request[_]): Future[Result] =
    Future.successful(badRequest)
}
