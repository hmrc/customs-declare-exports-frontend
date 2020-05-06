/*
 * Copyright 2020 HM Revenue & Customs
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

import controllers.actions.AuthAction
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Results}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.session_timed_out

import scala.concurrent.ExecutionContext

class SessionTimeoutController @Inject()(authenticate: AuthAction, mcc: MessagesControllerComponents, sessionTimedOut: session_timed_out)(
  implicit ec: ExecutionContext
) extends FrontendController(mcc) with I18nSupport {

  def signOut(): Action[AnyContent] = authenticate { implicit request =>
    Results.Redirect(routes.SessionTimeoutController.signedOut()).withNewSession
  }

  def signedOut(): Action[AnyContent] = Action { implicit request =>
    Ok(sessionTimedOut())
  }
}
