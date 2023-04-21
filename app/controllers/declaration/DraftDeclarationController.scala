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

package controllers.declaration

import config.AppConfig
import controllers.actions.{AuthAction, JourneyAction}
import models.requests.SessionHelper.{declarationUuid, errorFixModeSessionKey}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.draft_declaration_page

import javax.inject.Inject

class DraftDeclarationController @Inject() (
  authenticate: AuthAction,
  appConfig: AppConfig,
  mcc: MessagesControllerComponents,
  draftDeclarationPage: draft_declaration_page,
  journeyType: JourneyAction
) extends FrontendController(mcc) with I18nSupport {

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val updatedDateTime = request.cacheModel.declarationMeta.updatedDateTime
    val expiry = updatedDateTime.plusSeconds(appConfig.draftTimeToLive.toSeconds).toEpochMilli.toString

    Ok(draftDeclarationPage(request.declarationId, expiry)).removingFromSession(declarationUuid, errorFixModeSessionKey)
  }
}
