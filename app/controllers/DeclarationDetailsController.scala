/*
 * Copyright 2022 HM Revenue & Customs
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

import config.featureFlags.QueryNotificationMessageConfig
import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, VerifiedEmailAction}
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.{declaration_details, declaration_information}

class DeclarationDetailsController @Inject()(
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  mcc: MessagesControllerComponents,
  queryNotificationMessageConfig: QueryNotificationMessageConfig,
  declarationInformationPage: declaration_information,
  declarationDetailsPage: declaration_details
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(submissionId: String): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    customsDeclareExportsConnector.findSubmission(submissionId).flatMap {
      case Some(submission) =>
        customsDeclareExportsConnector.findNotifications(submissionId).map { notifications =>
          if (queryNotificationMessageConfig.isQueryNotificationMessageEnabled) Ok(declarationDetailsPage(submission, notifications))
          else Ok(declarationInformationPage(submission, notifications))
        }

      case _ => Future.successful(Redirect(routes.SubmissionsController.displayListOfSubmissions()))
    }
  }
}
