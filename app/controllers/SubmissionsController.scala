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

import connectors.CustomsDeclareExportsConnector
import connectors.exchange.ExportsDeclarationExchange
import controllers.actions.AuthAction
import controllers.util.SubmissionDisplayHelper
import javax.inject.Inject
import models.Mode
import models.requests.ExportsSessionKeys
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.submissions

import scala.concurrent.{ExecutionContext, Future}

class SubmissionsController @Inject()(
  authenticate: AuthAction,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  mcc: MessagesControllerComponents,
  submissionsPage: submissions
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayListOfSubmissions(): Action[AnyContent] = authenticate.async { implicit request =>
    for {
      submissions <- customsDeclareExportsConnector.fetchSubmissions()
      notifications <- customsDeclareExportsConnector.fetchNotifications()

      result = SubmissionDisplayHelper.createSubmissionsWithSortedNotificationsMap(submissions, notifications)

    } yield Ok(submissionsPage(result))
  }

  def amend(id: String): Action[AnyContent] = authenticate.async { implicit request =>
    customsDeclareExportsConnector.findDeclaration(id) flatMap {
      case Some(declaration) =>
        val amendedDeclaration = ExportsDeclarationExchange.withoutId(declaration.amend())
        customsDeclareExportsConnector
          .createDeclaration(amendedDeclaration)
          .map { created =>
            Redirect(controllers.declaration.routes.SummaryController.displayPage(Mode.Amend))
              .addingToSession(ExportsSessionKeys.declarationId -> created.id)
          }
      case _ => Future.successful(Redirect(routes.SubmissionsController.displayListOfSubmissions()))
    }

  }

}
