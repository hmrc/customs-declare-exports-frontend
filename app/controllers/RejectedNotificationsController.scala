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

import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, VerifiedEmailAction}
import handlers.ErrorHandler
import models.declaration.notifications.{Notification, NotificationError}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.rejected_notification_errors

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RejectedNotificationsController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  errorHandler: ErrorHandler,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  mcc: MessagesControllerComponents,
  rejectedNotificationPage: rejected_notification_errors
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with Logging {

  def displayPage(id: String): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    customsDeclareExportsConnector.findDeclaration(id).flatMap {
      case Some(declaration) =>
        customsDeclareExportsConnector.findNotifications(id).map { notifications =>
          val maybeMrn = notifications.headOption.map(_.mrn)
          Ok(rejectedNotificationPage(declaration, maybeMrn, getRejectedNotificationErrors(notifications)))
        }

      case _ => errorHandler.internalError(s"Declaration($id) not found??")
    }
  }

  def amendmentRejected(id: String, actionId: String): Action[AnyContent] = (authenticate andThen verifyEmail).async { _ =>
    throw new NotImplementedError()
  }

  private def getRejectedNotificationErrors(notifications: Seq[Notification]): Seq[NotificationError] =
    notifications.find(_.isStatusDMSRej).map(_.errors).getOrElse(Seq.empty)
}
