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
import controllers.general.routes.SignOutController
import models.SignOutReason
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.general.{session_timed_out, user_signed_out}

import javax.inject.Inject

class SignOutController @Inject() (
  mcc: MessagesControllerComponents,
  sessionTimedOut: session_timed_out,
  userSignedOutPage: user_signed_out,
  config: AppConfig
) extends FrontendController(mcc) with I18nSupport {

  def signOut(signOutReason: SignOutReason): Action[AnyContent] = Action { _ =>
    val redirectionTarget: Call = signOutReason match {
      case SignOutReason.SessionTimeout => SignOutController.sessionTimeoutSignedOut
      case SignOutReason.UserAction     => SignOutController.userSignedOut
    }
    Redirect(config.signOut, Map("continue" -> Seq(s"${config.selfBaseUrl.getOrElse("")}${redirectionTarget.url}")))
  }

  def sessionTimeoutSignedOut(): Action[AnyContent] = Action { implicit request =>
    Ok(sessionTimedOut())
  }

  def userSignedOut(): Action[AnyContent] = Action { implicit request =>
    Ok(userSignedOutPage())
  }

}
