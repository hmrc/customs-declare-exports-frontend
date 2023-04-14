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
import controllers.declaration.ConfirmationController._
import controllers.routes.RejectedNotificationsController
import handlers.ErrorHandler
import models.declaration.submissions.EnhancedStatus
import models.requests.{AuthenticatedRequest, ExportsSessionKeys}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.dashboard.DashboardHelper.toDashboard
import views.helpers.Confirmation
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
      Future.successful(Redirect(toDashboard))
    } { submissionId =>
      for {
        submission <- customsDeclareExportsConnector.findSubmission(submissionId)
        // To avoid failing entire Future for sake of getting a location code, recover to None
        declaration <- customsDeclareExportsConnector.findDeclaration(submissionId) recover { case _ => None }
        locationCode = declaration.flatMap(_.locations.goodsLocation).map(_.code)
      } yield submission match {
        case Some(submission) if submission.latestEnhancedStatus contains EnhancedStatus.ERRORS =>
          Redirect(RejectedNotificationsController.displayPage(submissionId, false))

        case _ =>
          val confirmation = Confirmation(request.email, extractDeclarationType, submission, locationCode)
          Ok(confirmationPage(confirmation))
      }
    }
  }

  private def extractDeclarationType(implicit request: AuthenticatedRequest[_]): String =
    request.session.data.get(ExportsSessionKeys.declarationType).fold("")(identity)

  private def extractSubmissionId(implicit request: AuthenticatedRequest[_]): Option[String] =
    request.session.data.get(ExportsSessionKeys.submissionId)

  private def hasNotification(implicit request: AuthenticatedRequest[_]): Future[Boolean] =
    extractSubmissionId.fold {
      logger.warn("Session on /holding does not include the submission's uuid!?")
      Future.successful(false)
    } {
      customsDeclareExportsConnector.findSubmission(_).map(_.fold(false)(_.latestEnhancedStatus.isDefined))
    }
}

object ConfirmationController {
  val js = "js"
  val Disabled = "disabled"
  val Enabled = "enabled"
}
