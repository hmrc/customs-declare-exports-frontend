/*
 * Copyright 2019 HM Revenue & Customs
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

import connectors.CustomsDeclareExportsConnector
import controllers.actions.AuthAction
import javax.inject.Inject
import models.requests.ExportsSessionKeys
import models.{Mode, Page}
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.saved_declarations

import scala.concurrent.{ExecutionContext, Future}

class SavedDeclarationsController @Inject()(
  authenticate: AuthAction,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  mcc: MessagesControllerComponents,
  savedDeclarationsPage: saved_declarations
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  private val logger = Logger(this.getClass)
  private val defaultPage = Page()

  def displayDeclarations(): Action[AnyContent] = authenticate.async { implicit request =>
    customsDeclareExportsConnector.findSavedDeclarations(defaultPage).map { page =>
      Ok(savedDeclarationsPage(page))
    }
  }

  def continueDeclaration(id: String): Action[AnyContent] = authenticate.async { implicit request =>
    customsDeclareExportsConnector.findDeclaration(id) flatMap {
      case Some(declaration) =>
        Future.successful(
          Redirect(controllers.declaration.routes.SummaryController.displayPage(Mode.SavedMode))
            .addingToSession(ExportsSessionKeys.declarationId -> id)
        )
      case _ => Future.successful(Redirect(controllers.routes.SavedDeclarationsController.displayDeclarations()))
    }

  }
}
