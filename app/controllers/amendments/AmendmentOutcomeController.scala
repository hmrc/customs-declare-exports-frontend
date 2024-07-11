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

package controllers.amendments

import config.featureFlags.DeclarationAmendmentsConfig
import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, VerifiedEmailAction}
import controllers.amendments.routes
import controllers.general.ErrorHandler
import controllers.general.routes.RootController
import models.declaration.submissions.EnhancedStatus.{CUSTOMS_POSITION_DENIED, CUSTOMS_POSITION_GRANTED, ERRORS}
import models.declaration.submissions.RequestType.AmendmentCancellationRequest
import models.declaration.submissions.{Action => Actn, Submission}
import models.requests.SessionHelper._
import models.requests.{AuthenticatedRequest, VerifiedEmailRequest}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.helpers.Confirmation
import views.helpers.ConfirmationHelper._
import views.html.amendments._
import views.html.summary.holding_page

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendmentOutcomeController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  mcc: MessagesControllerComponents,
  errorHandler: ErrorHandler,
  holdingPage: holding_page,
  amendment_accepted: amendment_accepted,
  amendment_cancelled: amendment_cancelled,
  amendment_rejection: amendment_rejection,
  amendment_failed: amendment_failed,
  amendment_pending: amendment_pending,
  declarationAmendmentsConfig: DeclarationAmendmentsConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with Logging {

  def displayHoldingPage(isCancellation: Boolean): Action[AnyContent] = authenticate.async { implicit request =>
    val title = if (isCancellation) "declaration.cancel.amendment.holding.title" else "declaration.amendment.holding.title"

    request.getQueryString(js) match {

      // Show page at /holding and wait a few secs.
      case None =>
        val holdingUrl = routes.AmendmentOutcomeController.displayHoldingPage(isCancellation).url
        val redirectToUrl = routes.AmendmentOutcomeController.displayOutcomePage.url
        Future.successful(Ok(holdingPage(redirectToUrl, s"$holdingUrl?$js=$Disabled", s"$holdingUrl?$js=$Enabled", title)))

      // Javascript disabled. 1st check if at least 1 notification was sent in response of the submission.
      case Some(Disabled) =>
        val redirectToUrl = routes.AmendmentOutcomeController.displayOutcomePage
        hasNotification.map {
          case true  => Redirect(redirectToUrl)
          case false => Ok(holdingPage(redirectToUrl.url, redirectToUrl.url, "", title))
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

  val displayOutcomePage: Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    if (!declarationAmendmentsConfig.isEnabled) Future.successful(Redirect(RootController.displayPage))
    else retrieveIdsFromSessionAndDisplayOutcomePage
  }

  private def retrieveIdsFromSessionAndDisplayOutcomePage(implicit request: VerifiedEmailRequest[AnyContent]): Future[Result] =
    (getValue(submissionUuid), getValue(submissionActionId)) match {
      case (Some(submissionId), Some(actionId)) =>
        retrieveSubmissionAndDisplayOutcomePage(submissionId, actionId)

      case _ =>
        val msg = "Session on /amendment-outcome does not include the submissionUuid and/or the actionId!?"
        errorHandler.internalError(msg)
    }

  private def retrieveSubmissionAndDisplayOutcomePage(submissionId: String, actionId: String)(
    implicit request: VerifiedEmailRequest[_]
  ): Future[Result] =
    customsDeclareExportsConnector.findSubmission(submissionId).flatMap {
      case Some(submission) =>
        submission.actions.find(_.id == actionId) match {
          case Some(action) => displayOutcomePage(submission, action)

          case _ =>
            val msg = s"Submission($submissionId), fetched via Session, doesn't have a Action($actionId), also fetched via Session!?"
            errorHandler.internalError(msg)
        }

      case _ =>
        val msg = s"Submission($submissionId), via Session key, not found while trying to display an amendment outcome!?"
        errorHandler.internalError(msg)
    }

  private def displayOutcomePage(submission: Submission, action: Actn)(implicit request: VerifiedEmailRequest[_]): Future[Result] =
    retrieveLocationCode(submission).flatMap {
      case Right(confirmation) =>
        val page = action.latestNotificationSummary match {
          case Some(notification) if notification.enhancedStatus == ERRORS                  => amendment_rejection(confirmation)
          case Some(notification) if notification.enhancedStatus == CUSTOMS_POSITION_DENIED => amendment_failed(confirmation)

          case Some(notification) if notification.enhancedStatus == CUSTOMS_POSITION_GRANTED =>
            if (action.requestType == AmendmentCancellationRequest) amendment_cancelled(confirmation)
            else amendment_accepted(confirmation)

          case _ => amendment_pending(confirmation)
        }
        Future.successful(Ok(page))

      case Left(message) => errorHandler.internalError(message)
    }

  private def hasNotification(implicit request: AuthenticatedRequest[_]): Future[Boolean] =
    getValue(submissionActionId)
      .map(customsDeclareExportsConnector.findAction(_).map(_.exists(_.latestNotificationSummary.isDefined)))
      .getOrElse {
        logger.warn("Session on /amendment-holding does not include the actionId!?")
        Future.successful(false)
      }

  private def retrieveLocationCode(submission: Submission)(implicit request: VerifiedEmailRequest[_]): Future[Either[String, Confirmation]] =
    customsDeclareExportsConnector
      .findDeclaration(submission.uuid)
      .map {
        case Some(declaration) =>
          declaration.additionalDeclarationType.fold[Either[String, Confirmation]] {
            Left(s"Declaration(${declaration.id}) without 'additionalDeclarationType' after the submission of an amendment??")
          } { additionalDeclarationType =>
            val maybeLocationCode = declaration.locations.goodsLocation.map(_.value)
            Right(Confirmation(request.email, additionalDeclarationType.toString, submission, maybeLocationCode))
          }

        case _ => Left(s"Declaration(${submission.uuid}) not found after the submission of an amendment??")
      }
}
