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

import config.featureFlags.TdrFeatureFlags
import models.UnauthorisedReason
import models.UnauthorisedReason._
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.general.{unauthorised, unauthorisedAgent, unauthorisedEoriInTdr}

import javax.inject.Inject

class UnauthorisedController @Inject() (
  tdrFeatureFlags: TdrFeatureFlags,
  mcc: MessagesControllerComponents,
  unauthorisedPage: unauthorised,
  unauthorisedEoriInTdrPage: unauthorisedEoriInTdr,
  unauthorisedAgent: unauthorisedAgent
) extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(unauthorisedReason: UnauthorisedReason): Action[AnyContent] = Action { implicit request =>
    val tdrFlagEnabled = tdrFeatureFlags.isTdrUnauthorisedMessageEnabled

    unauthorisedReason match {
      case _ if tdrFlagEnabled => Ok(unauthorisedEoriInTdrPage())
      case UserEoriNotAllowed  => Ok(unauthorisedPage(displaySignOut = true))
      case UserIsNotEnrolled   => Ok(unauthorisedPage(displaySignOut = true))
      case UrlDirect           => Ok(unauthorisedPage(displaySignOut = false))
    }
  }

  def onAgentKickOut(unauthorisedReason: UnauthorisedReason): Action[AnyContent] = Action { implicit request =>
    unauthorisedReason match {
      case UserIsAgent => Ok(unauthorisedAgent(displaySignOut = true))
      case _           => Ok(unauthorisedAgent(displaySignOut = false))
    }
  }
}
