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
import models.requests.SessionHelper.{getValue, submissionActionId, submissionUuid}
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
          val messages = messagesApi.preferred(request).messages
          val mrn = notifications.headOption.map(_.mrn).getOrElse(messages("rejected.notification.mrn.missing"))
          Ok(rejectedNotificationPage(None, declaration, mrn, None, getRejectedNotificationErrors(notifications)))
        }

      case _ => errorHandler.internalError(s"Declaration($id) not found for a rejected submission??")
    }
  }

  def displayPageOnUnacceptedAmendment(actionId: String, draftDeclarationId: Option[String] = None): Action[AnyContent] =
    (authenticate andThen verifyEmail).async { implicit request =>
      customsDeclareExportsConnector.findAction(actionId).flatMap {
        case Some(action) =>
          action.decId match {
            case Some(decId) =>
              val declarationId = draftDeclarationId.fold(decId)(identity)
              customsDeclareExportsConnector.findDeclaration(declarationId).flatMap {
                case Some(declaration) =>
                  customsDeclareExportsConnector.findLatestNotification(actionId).map {
                    case Some(notification) =>
                      Ok(rejectedNotificationPage(getValue(submissionUuid), declaration, notification.mrn, Some(declarationId), notification.errors))
                        .addingToSession(submissionActionId -> actionId)

                    case _ => errorHandler.internalServerError(s"Failed|rejected amended Notification not found for Action($actionId)??")
                  }

                case _ =>
                  val draft = draftDeclarationId.fold("")(_ => "(draft) ")
                  errorHandler.internalError(s"Failed|rejected amended ${draft}declaration($declarationId) not found for Action($actionId)??")
              }

            case _ => errorHandler.internalError(s"The Action($actionId) does not have decId for a failed|rejected amendment??")
          }

        case _ => errorHandler.internalError(s"Action($actionId) not found for a failed|rejected amendment??")
      }
    }

  private def getRejectedNotificationErrors(notifications: Seq[Notification]): Seq[NotificationError] =
    notifications.find(_.isStatusDMSRej).map(_.errors).getOrElse(Seq.empty)
}
