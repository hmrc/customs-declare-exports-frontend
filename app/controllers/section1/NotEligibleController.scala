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

package controllers.section1

import controllers.actions.AuthAction
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section1.{not_declarant, not_eligible}

import javax.inject.Inject

class NotEligibleController @Inject() (
  authenticate: AuthAction,
  mcc: MessagesControllerComponents,
  notEligiblePage: not_eligible,
  notDeclarantPage: not_declarant
) extends FrontendController(mcc) with I18nSupport {

  def displayNotEligible(): Action[AnyContent] = authenticate { implicit request =>
    Ok(notEligiblePage())
  }

  def displayNotDeclarant(): Action[AnyContent] = authenticate { implicit request =>
    Ok(notDeclarantPage())
  }
}
