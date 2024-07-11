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

package controllers.timeline

import connectors.{CodeListConnector, CustomsDeclareExportsConnector}
import controllers.actions.{AuthAction, VerifiedEmailAction}
import controllers.general.ErrorHandler
import models.requests.SessionHelper.{getValue, submissionActionId, submissionUuid}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.helpers.ErrorsReportedHelper
import views.html.timeline.errors_reported

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RejectedNotificationsController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  errorHandler: ErrorHandler,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  codeListConnector: CodeListConnector,
  mcc: MessagesControllerComponents,
  errorsReportedHelper: ErrorsReportedHelper,
  errors_reported: errors_reported
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with Logging {

  def displayPage(id: String): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    val declarationsAndNotificationsInvolved = for {
      declaration <- customsDeclareExportsConnector.findDeclaration(id)
      draftInProgress <- customsDeclareExportsConnector.findDraftByParent(id)
      notifications <- customsDeclareExportsConnector.findNotifications(id)
    } yield (declaration, draftInProgress, notifications)

    declarationsAndNotificationsInvolved.map {
      case (Some(declaration), maybeDraftInProgress, notifications) =>
        val messages = messagesApi.preferred(request).messages
        val mrn = notifications.headOption.map(_.mrn).getOrElse(messages("rejected.notification.mrn.missing"))

        val rejectionNotification = notifications.find(_.isStatusDMSRej).headOption
        val errors = errorsReportedHelper.generateErrorRows(rejectionNotification, declaration, maybeDraftInProgress, false)

        Ok(errors_reported(None, declaration, mrn, None, errors)(request, messages, codeListConnector))

      case _ => errorHandler.internalServerError(s"Declaration($id) not found for a rejected submission??")
    }
  }

  def displayPageOnUnacceptedAmendment(actionId: String, draftDeclarationId: Option[String] = None): Action[AnyContent] =
    (authenticate andThen verifyEmail).async { implicit request =>
      val messages = messagesApi.preferred(request).messages

      customsDeclareExportsConnector.findAction(actionId).flatMap {
        case Some(action) =>
          action.decId match {
            case Some(decId) =>
              val id = draftDeclarationId.fold(decId)(identity)

              val declarationsAndNotificationsInvolved = for {
                declaration <- customsDeclareExportsConnector.findDeclaration(id)
                draftInProgress <- customsDeclareExportsConnector.findDraftByParent(id)
                notifications <- customsDeclareExportsConnector.findLatestNotification(actionId)
              } yield (declaration, draftInProgress, notifications)

              declarationsAndNotificationsInvolved.flatMap {
                case (Some(declaration), maybeDraftInProgress, Some(notification)) =>
                  val errors = errorsReportedHelper.generateErrorRows(Some(notification), declaration, maybeDraftInProgress, true)

                  Future.successful(
                    Ok(
                      errors_reported(getValue(submissionUuid), declaration, notification.mrn, Some(id), errors)(request, messages, codeListConnector)
                    )
                      .addingToSession(submissionActionId -> actionId)
                  )

                case (Some(_), _, None) =>
                  errorHandler.internalError(s"Failed|rejected amended Notification not found for Action($actionId)??")

                case _ =>
                  val draft = draftDeclarationId.fold("")(_ => "(draft) ")
                  errorHandler.internalError(s"Failed|rejected amended ${draft}declaration($id) not found for Action($actionId)??")
              }

            case _ => errorHandler.internalError(s"The Action($actionId) does not have decId for a failed|rejected amendment??")
          }

        case _ => errorHandler.internalError(s"Action($actionId) not found for a failed|rejected amendment??")
      }
    }
}
