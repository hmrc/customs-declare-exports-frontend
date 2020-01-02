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

import config.AppConfig
import connectors.CustomsDeclareExportsConnector
import controllers.actions.AuthAction
import javax.inject.Inject
import models.requests.ExportsSessionKeys
import models.{Mode, Page}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.saved_declarations

import scala.concurrent.{ExecutionContext, Future}

class SavedDeclarationsController @Inject()(
  authenticate: AuthAction,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  mcc: MessagesControllerComponents,
  savedDeclarationsPage: saved_declarations,
  appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayDeclarations(pageNumber: Int = 1): Action[AnyContent] = authenticate.async { implicit request =>
    customsDeclareExportsConnector.findSavedDeclarations(Page(pageNumber, appConfig.paginationItemsPerPage)).map { page =>
      Ok(savedDeclarationsPage(page))
    }
  }

  def continueDeclaration(id: String): Action[AnyContent] = authenticate.async { implicit request =>
    customsDeclareExportsConnector.findDeclaration(id) flatMap {
      case Some(_) =>
        Future.successful(
          Redirect(controllers.declaration.routes.SummaryController.displayPage(Mode.Draft))
            .addingToSession(ExportsSessionKeys.declarationId -> id)
        )
      case None => Future.successful(Redirect(controllers.routes.SavedDeclarationsController.displayDeclarations()))
    }

  }
}
