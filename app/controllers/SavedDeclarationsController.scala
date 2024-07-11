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

import scala.concurrent.{ExecutionContext, Future}

import config.PaginationConfig
import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, VerifiedEmailAction}
import controllers.routes.SavedDeclarationsController
import controllers.summary.routes.SummaryController
import javax.inject.Inject
import models.requests.SessionHelper
import models.Page
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.drafts.saved_declarations

class SavedDeclarationsController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  mcc: MessagesControllerComponents,
  draftDeclarations: saved_declarations,
  paginationConfig: PaginationConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayDeclarations(pageNumber: Int = 1): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    customsDeclareExportsConnector.fetchDraftDeclarations(Page(pageNumber, paginationConfig.itemsPerPage)).map { page =>
      Ok(draftDeclarations(page))
    }
  }

  def displayDeclaration(id: String): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    customsDeclareExportsConnector.findDeclaration(id) flatMap {
      case Some(_) => Future.successful(Redirect(SummaryController.displayPage).addingToSession(SessionHelper.declarationUuid -> id))
      case None    => Future.successful(Redirect(SavedDeclarationsController.displayDeclarations()))
    }
  }
}
