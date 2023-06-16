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

package controllers.declaration

import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, VerifiedEmailAction}
import controllers.routes.RejectedNotificationsController
import handlers.ErrorHandler
import models.declaration.submissions.RequestType.SubmissionRequest
import models.declaration.submissions.{EnhancedStatus, Submission}
import models.requests.SessionHelper.{getValue, submissionUuid}
import models.requests.{AuthenticatedRequest, VerifiedEmailRequest}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.helpers.Confirmation
import views.helpers.ConfirmationHelper._
import views.html.declaration.confirmation.{confirmation_page, holding_page}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmationController @Inject() (
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
        val redirectToUrl = routes.ConfirmationController.displayConfirmationPage.url
        Future.successful(Ok(holdingPage(redirectToUrl, s"$holdingUrl?$js=$Disabled", s"$holdingUrl?$js=$Enabled")))

      // Javascript disabled. 1st check if at least 1 notification was sent in response of the submission.
      case Some(Disabled) =>
        val redirectToUrl = routes.ConfirmationController.displayConfirmationPage
        hasNotification.map {
          case true  => Redirect(redirectToUrl)
          case false => Ok(holdingPage(redirectToUrl.url, redirectToUrl.url, ""))
        }

      // Javascript enabled. 1st check if at least 1 notification was sent in response of the submission.
      case Some(Enabled) =>
        hasNotification.map {
          case true  => Ok("found")
          case false => NotFound("not confirmed yet")
        }

      case Some(_) =>
        logger.warn("Unknown value for query parameter 'js'. Can only be 'disabled' or 'enabled'.")
        errorHandler.redirectToErrorPage
    }
  }

  val displayConfirmationPage: Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    getValue(submissionUuid).fold {
      errorHandler.internalError("Session on /confirmation does not include the submission's uuid!?")
    } { submissionId =>
      customsDeclareExportsConnector.findSubmission(submissionId).flatMap {
        case Some(submission) if submission.latestEnhancedStatus contains EnhancedStatus.ERRORS =>
          Future.successful(Redirect(RejectedNotificationsController.displayPage(submissionId)))

        case Some(submission) =>
          retrieveLocationCode(submission).flatMap {
            case Right(confirmation) => Future.successful(Ok(confirmationPage(confirmation)))
            case Left(message)       => errorHandler.internalError(message)
          }

        case _ =>
          errorHandler.internalError(s"Submission($submissionId) not found after the holding page??")
      }
    }
  }

  private def hasNotification(implicit request: AuthenticatedRequest[_]): Future[Boolean] = {
    def hasNotificationForSubmissionRequest(submission: Submission): Boolean =
      submission.actions.exists(action => action.requestType == SubmissionRequest && action.latestNotificationSummary.isDefined)

    getValue(submissionUuid)
      .map(customsDeclareExportsConnector.findSubmission(_).map(_.exists(hasNotificationForSubmissionRequest)))
      .getOrElse {
        logger.warn("Session on /holding does not include the submission's uuid!?")
        Future.successful(false)
      }
  }

  private def retrieveLocationCode(submission: Submission)(implicit request: VerifiedEmailRequest[_]): Future[Either[String, Confirmation]] =
    customsDeclareExportsConnector
      .findDeclaration(submission.uuid)
      .map {
        case Some(declaration) =>
          declaration.additionalDeclarationType.fold[Either[String, Confirmation]] {
            Left(s"Declaration(${declaration.id}) without 'additionalDeclarationType' after having been submitted??")
          } { additionalDeclarationType =>
            val maybeLocationCode = declaration.locations.goodsLocation.map(_.code)
            Right(Confirmation(request.email, additionalDeclarationType.toString, submission, maybeLocationCode))
          }

        case _ => Left(s"Declaration(${submission.uuid}) not found after having been submitted??")
      }
}
