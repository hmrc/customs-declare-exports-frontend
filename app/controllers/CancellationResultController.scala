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

import connectors.CustomsDeclareExportsConnector
import controllers.CancellationResultController.{js, Disabled, Enabled}
import controllers.actions.{AuthAction, VerifiedEmailAction}
import handlers.ErrorHandler
import models.declaration.submissions.EnhancedStatus.{CUSTOMS_POSITION_DENIED, CUSTOMS_POSITION_GRANTED}
import models.declaration.submissions.NotificationSummary
import models.requests.AuthenticatedRequest
import models.requests.ExportsSessionKeys.submissionMrn
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result, Session}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.{cancellation_holding, cancellation_result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CancellationResultController @Inject()(
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
    extractMrnFromSession { mrn =>
      request.getQueryString(js) match {
        // Show page at /holding and wait a few secs.
        case None =>
          val holdingUrl = routes.CancellationResultController.displayHoldingPage.url
          Future.successful(Ok(cancelHoldingPage(s"$holdingUrl?$js=$Disabled", s"$holdingUrl?$js=$Enabled", mrn)))

        // Javascript disabled. 1st check if at least 1 notification was sent in response of the submission.
        case Some(Disabled) =>
          hasNotificationSummary(mrn).map {
            case true =>
              Redirect(routes.CancellationResultController.displayResultPage())
            case false => Ok(cancelHoldingPage(routes.CancellationResultController.displayResultPage.url, "", mrn))
          }

        // Javascript enabled. 1st check if at least 1 notification was sent in response of the submission.
        case Some(Enabled) =>
          hasNotificationSummary(mrn).map {
            case true  => Ok("found")
            case false => NotFound("not confirmed yet")
          }

        case Some(_) =>
          logger.warn("Unknown value for query parameter 'js'. Can only be 'disabled' or 'enabled'.")
          errorHandler.displayErrorPage
      }
    }
  }

  val displayResultPage: Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    extractMrnFromSession(getResultPage)
  }

  private def getResultPage(mrn: String)(implicit request: AuthenticatedRequest[_]): Future[Result] =
    getNotificationSummary(mrn)
      .map(_.fold {
        logger.debug("No notifications found for MRN")
        Ok(cancellationResultPage(None, mrn)).withSession(mrnRemoved)
      } { notification =>
        notification.enhancedStatus match {
          case CUSTOMS_POSITION_DENIED  => Ok(cancellationResultPage(Some(notification.enhancedStatus), mrn)).withSession(mrnRemoved)
          case CUSTOMS_POSITION_GRANTED => Ok(cancellationResultPage(Some(notification.enhancedStatus), mrn)).withSession(mrnRemoved)
          case _                        => Ok(cancellationResultPage(None, mrn)).withSession(mrnRemoved)
        }
      })

  private def extractMrnFromSession(f: String => Future[Result])(implicit request: AuthenticatedRequest[_]): Future[Result] =
    request.session
      .get(submissionMrn)
      .fold {
        logger.warn("No MRN found in session")
        errorHandler.displayErrorPage
      }(f)

  private def mrnRemoved(implicit request: AuthenticatedRequest[_]): Session =
    request.session - submissionMrn

  private def getNotificationSummary(mrn: String)(implicit request: AuthenticatedRequest[_]): Future[Option[NotificationSummary]] =
    customsDeclareExportsConnector
      .findSubmissionByMrn(mrn)
      .map { maybeSubmission =>
        for {
          submission <- maybeSubmission
          action <- submission.latestCancellationAction
          notificationSummary <- action.latestNotificationSummary
        } yield notificationSummary
      }

  private def hasNotificationSummary(mrn: String)(implicit request: AuthenticatedRequest[_]): Future[Boolean] =
    getNotificationSummary(mrn).map(_.isDefined)
}

object CancellationResultController {
  val js = "js"
  val Disabled = "disabled"
  val Enabled = "enabled"
}
