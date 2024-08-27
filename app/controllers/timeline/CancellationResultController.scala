/*
 * Copyright 2024 HM Revenue & Customs
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

import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, VerifiedEmailAction}
import controllers.general.ErrorHandler
import models.declaration.submissions.EnhancedStatus.{CUSTOMS_POSITION_DENIED, CUSTOMS_POSITION_GRANTED, EnhancedStatus}
import models.declaration.submissions.NotificationSummary
import models.requests.AuthenticatedRequest
import models.requests.SessionHelper.{getValue, removeValue, submissionMrn, submissionUuid}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.timeline.{cancellation_holding, cancellation_result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CancellationResultController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  mcc: MessagesControllerComponents,
  errorHandler: ErrorHandler,
  cancelHoldingPage: cancellation_holding,
  cancellationResultPage: cancellation_result
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with Logging {

  val displayHoldingPage: Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    extractDataFromSession { (submissionId, mrn) =>
      request.getQueryString(js) match {
        // Show page at /holding and wait a few secs.
        case None =>
          val holdingUrl = routes.CancellationResultController.displayHoldingPage.url
          Future.successful(Ok(cancelHoldingPage(s"$holdingUrl?$js=$Disabled", s"$holdingUrl?$js=$Enabled", mrn)))

        // Javascript disabled. 1st check if at least 1 notification was sent in response of the submission.
        case Some(Disabled) =>
          hasCancellationNotificationSummary(submissionId).map {
            case true  => Redirect(routes.CancellationResultController.displayResultPage)
            case false => Ok(cancelHoldingPage(routes.CancellationResultController.displayResultPage.url, "", mrn))
          }

        // Javascript enabled. 1st check if at least 1 notification was sent in response of the submission.
        case Some(Enabled) =>
          hasCancellationNotificationSummary(submissionId).map {
            case true  => Ok("found")
            case false => NotFound("not confirmed yet")
          }

        case Some(_) =>
          logger.warn("Unknown value for query parameter 'js'. Can only be 'disabled' or 'enabled'.")
          errorHandler.redirectToErrorPage
      }
    }
  }

  val displayResultPage: Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    extractDataFromSession { (submissionId, mrn) =>
      def result(status: Option[EnhancedStatus]) = Ok(cancellationResultPage(status, mrn)).withSession(removeValue(submissionUuid))

      getLatestCancellationNotificationSummary(submissionId)
        .map(_.fold {
          logger.debug("No notifications found for SubmissionId")
          result(None)
        } { notification =>
          notification.enhancedStatus match {
            case CUSTOMS_POSITION_DENIED | CUSTOMS_POSITION_GRANTED => result(Some(notification.enhancedStatus))
            case _                                                  => result(None)
          }
        })
    }
  }

  private def extractDataFromSession(f: (String, String) => Future[Result])(implicit request: AuthenticatedRequest[_]): Future[Result] =
    (for {
      submissionId <- getValue(submissionUuid)
      mrn <- getValue(submissionMrn)
    } yield (submissionId, mrn)).fold {
      logger.warn("No submissionUuid and/or mrn found in session")
      errorHandler.redirectToErrorPage
    } { case (submissionId, mrn) =>
      f(submissionId, mrn)
    }

  // TODO: need to refactor to get notificationSummary for submissionUuid
  private def getLatestCancellationNotificationSummary(
    submissionId: String
  )(implicit request: AuthenticatedRequest[_]): Future[Option[NotificationSummary]] =
    customsDeclareExportsConnector
      .findSubmission(submissionId)
      .map { maybeSubmission =>
        for {
          submission <- maybeSubmission
          action <- submission.latestCancellationAction
          notificationSummary <- action.latestNotificationSummary
        } yield notificationSummary
      }

  private def hasCancellationNotificationSummary(submissionId: String)(implicit request: AuthenticatedRequest[_]): Future[Boolean] =
    getLatestCancellationNotificationSummary(submissionId).map(_.isDefined)

  private val js = "js"
  private val Disabled = "disabled"
  private val Enabled = "enabled"
}
