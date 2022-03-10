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

package controllers.declaration

import scala.concurrent.{ExecutionContext, Future}

import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, VerifiedEmailAction}
import controllers.declaration.ConfirmationController._
import controllers.routes.{RejectedNotificationsController, SubmissionsController}
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.{AuthenticatedRequest, ExportsSessionKeys}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.helpers.Confirmation
import views.html.declaration.confirmation.{confirmation_page, holding_page}

class ConfirmationController @Inject()(
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  mcc: MessagesControllerComponents,
  errorHandler: ErrorHandler,
  holdingPage: holding_page,
  confirmationPage: confirmation_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with Logging {

  val displayHoldingPage: Action[AnyContent] = authenticate.async { implicit request =>
    request.getQueryString(js) match {

      // Show page at /holding and wait a few secs.
      case None =>
        val holdingUrl = routes.ConfirmationController.displayHoldingPage.url
        Future.successful(Ok(holdingPage(s"$holdingUrl?$js=$Disabled", s"$holdingUrl?$js=$Enabled")))

      // Javascript disabled. 1st check if at least 1 notification was sent in response of the submission.
      case Some(Disabled) =>
        hasNotification.map {
          case true  => Redirect(routes.ConfirmationController.displayConfirmationPage)
          case false => Ok(holdingPage(routes.ConfirmationController.displayConfirmationPage.url, ""))
        }

      // Javascript enabled. 1st check if at least 1 notification was sent in response of the submission.
      case Some(Enabled) =>
        hasNotification.map {
          case true  => Ok("found")
          case false => NotFound("not confirmed yet")
        }

      case Some(_) =>
        logger.warn("Unknown value for query parameter 'js'. Can only be 'disabled' or 'enabled'.")
        errorHandler.displayErrorPage
    }
  }

  val displayConfirmationPage: Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    extractSubmissionId.fold {
      logger.warn("Session on /confirmation does not include the submission's uuid!?")
      Future.successful(Redirect(SubmissionsController.displayListOfSubmissions()))
    } { submissionId =>
      customsDeclareExportsConnector.findLatestNotification(submissionId).flatMap {
        case Some(notification) if notification.isStatusDMSRej =>
          Future.successful(Redirect(RejectedNotificationsController.displayPage(submissionId)))

        case Some(notification) =>
          val confirmation = Confirmation(request.email, submissionId, extractDeclarationType, extractDucr, extractLrn, Some(notification))
          Future.successful(Ok(confirmationPage(confirmation)))

        case _ =>
          val confirmation = Confirmation(request.email, submissionId, extractDeclarationType, extractDucr, extractLrn, None)
          Future.successful(Ok(confirmationPage(confirmation)))
      }
    }
  }

  private def extractDeclarationType(implicit request: AuthenticatedRequest[_]): String =
    request.session.data.get(ExportsSessionKeys.declarationType).fold("")(identity)

  private def extractDucr(implicit request: AuthenticatedRequest[_]): Option[String] =
    request.session.data.get(ExportsSessionKeys.submissionDucr).map(_.trim).filter(_.nonEmpty)

  private def extractLrn(implicit request: AuthenticatedRequest[_]): Option[String] =
    request.session.data.get(ExportsSessionKeys.submissionLrn)

  private def extractSubmissionId(implicit request: AuthenticatedRequest[_]): Option[String] =
    request.session.data.get(ExportsSessionKeys.submissionId)

  private def hasNotification(implicit request: AuthenticatedRequest[_]): Future[Boolean] =
    extractSubmissionId.fold {
      logger.warn("Session on /holding does not include the submission's uuid!?")
      Future.successful(false)
    } {
      customsDeclareExportsConnector.findLatestNotification(_).map(_.isDefined)
    }
}

object ConfirmationController {
  val js = "js"
  val Disabled = "disabled"
  val Enabled = "enabled"
}
