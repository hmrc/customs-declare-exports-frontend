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

package controllers.drafts

import config.AppConfig
import controllers.actions.{AuthAction, JourneyAction}
import models.requests.SessionHelper.{declarationUuid, errorFixModeSessionKey}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.drafts.exit_and_complete_later

import javax.inject.Inject

class ExitAndCompleteLaterController @Inject() (
  authenticate: AuthAction,
  appConfig: AppConfig,
  mcc: MessagesControllerComponents,
  exitAndCompleteLater: exit_and_complete_later,
  journeyType: JourneyAction
) extends FrontendController(mcc) with I18nSupport {

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val updatedDateTime = request.cacheModel.declarationMeta.updatedDateTime
    val expiry = updatedDateTime.plusSeconds(appConfig.draftTimeToLive.toSeconds).toEpochMilli.toString

    Ok(exitAndCompleteLater(request.declarationId, expiry)).removingFromSession(declarationUuid, errorFixModeSessionKey)
  }
}
