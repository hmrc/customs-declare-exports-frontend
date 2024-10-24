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

import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, VerifiedEmailAction}
import controllers.routes.SavedDeclarationsController
import forms.drafts.RemoveDraftDeclaration.form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.drafts.remove_declaration

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveDraftDeclarationController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  mcc: MessagesControllerComponents,
  removeDeclarationPage: remove_declaration
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithUnsafeDefaultFormBinding {

  def displayPage(id: String): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    customsDeclareExportsConnector.findDeclaration(id) flatMap {
      case Some(declaration) => Future.successful(Ok(removeDeclarationPage(declaration, form)))
      case _                 => Future.successful(Redirect(SavedDeclarationsController.displayDeclarations()))
    }
  }

  def removeDeclaration(id: String): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          customsDeclareExportsConnector.findDeclaration(id) flatMap {
            case Some(declaration) => Future.successful(BadRequest(removeDeclarationPage(declaration, formWithErrors)))
            case _                 => Future.successful(Redirect(SavedDeclarationsController.displayDeclarations()))
          },
        validAction =>
          if (validAction.remove)
            customsDeclareExportsConnector
              .deleteDraftDeclaration(id)
              .map(_ => Redirect(SavedDeclarationsController.displayDeclarations()))
          else Future.successful(Redirect(SavedDeclarationsController.displayDeclarations()))
      )
  }
}
