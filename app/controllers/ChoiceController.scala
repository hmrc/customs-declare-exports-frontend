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

package controllers

import controllers.actions.{AuthAction, VerifiedEmailAction}
import models.requests.SessionHelper.{declarationUuid, errorFixModeSessionKey, errorKey}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.choice_page

import javax.inject.Inject

class ChoiceController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  mcc: MessagesControllerComponents,
  choicePage: choice_page
) extends FrontendController(mcc) with I18nSupport with WithUnsafeDefaultFormBinding {

  val displayPage: Action[AnyContent] = (authenticate andThen verifyEmail) { implicit request =>
    Ok(choicePage()).removingFromSession(declarationUuid, errorFixModeSessionKey, errorKey)
  }
}
