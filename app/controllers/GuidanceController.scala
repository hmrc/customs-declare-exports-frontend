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

import controllers.actions.AuthAction
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.guidance._

class GuidanceController @Inject() (
  authenticate: AuthAction,
  mcc: MessagesControllerComponents,
  completeDeclarationPage: complete_declaration,
  sendByRoroPage: send_by_roro,
  entryPage: entry
) extends FrontendController(mcc) with I18nSupport {

  val entry: Action[AnyContent] = authenticate { implicit request =>
    Ok(entryPage())
  }

  val completeDeclaration: Action[AnyContent] = authenticate { implicit request =>
    Ok(completeDeclarationPage())
  }

  val sendByRoro: Action[AnyContent] = authenticate { implicit request =>
    Ok(sendByRoroPage())
  }
}
